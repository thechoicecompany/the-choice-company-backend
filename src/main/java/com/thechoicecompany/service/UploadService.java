package com.thechoicecompany.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.thechoicecompany.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadService {

    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/webp",
        "image/gif"
    );

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5 MB

    /**
     * Upload one image to Cloudinary.
     *
     * @param file   MultipartFile from HTTP request
     * @param folder Cloudinary folder, e.g. "tcc/products" or "tcc/gallery"
     * @return Cloudinary secure HTTPS URL
     */
    public String uploadImage(MultipartFile file, String folder) {
        validateFile(file);

        String normalizedFolder = normalizeFolder(folder);

        return doUpload(file, normalizedFolder);
    }

    /**
     * Upload multiple images to Cloudinary sequentially.
     *
     * If any upload fails, previously uploaded images are deleted.
     *
     * @param files  List of MultipartFile images
     * @param folder Cloudinary folder
     * @return List of { url, publicId }
     */
    public List<Map<String, String>> uploadImages(
        List<MultipartFile> files,
        String folder
    ) {
        String normalizedFolder = normalizeFolder(folder);

        List<Map<String, String>> results = new ArrayList<>();
        List<String> publicIds = new ArrayList<>();

        try {
            for (MultipartFile file : files) {

                validateFile(file);

                UploadResult uploadResult =
                    doUploadWithPublicId(file, normalizedFolder);

                results.add(
                    Map.of(
                        "url", uploadResult.url(),
                        "publicId", uploadResult.publicId()
                    )
                );

                publicIds.add(uploadResult.publicId());

                log.info(
                    "Bulk upload {}/{} complete: {}",
                    results.size(),
                    files.size(),
                    uploadResult.url()
                );
            }

        } catch (Exception ex) {

            log.error(
                "Bulk upload failed at image {}/{} — rolling back {} uploads",
                results.size() + 1,
                files.size(),
                publicIds.size(),
                ex
            );

            publicIds.forEach(this::deleteImageQuietly);

            throw new BusinessException(
                "Image upload failed: " + ex.getMessage()
            );
        }

        return results;
    }

    /**
     * Delete an image from Cloudinary by public_id.
     *
     * Non-fatal — logs warning on failure.
     */
    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {

            cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.asMap(
                    "resource_type", "image"
                )
            );

            log.info(
                "Cloudinary image deleted: {}",
                publicId
            );

        } catch (IOException e) {

            log.warn(
                "Could not delete Cloudinary image {}: {}",
                publicId,
                e.getMessage()
            );
        }
    }

    // ─────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────

    /**
     * Validate uploaded file.
     */
    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BusinessException(
                "File is empty or missing"
            );
        }

        String mime = file.getContentType();

        if (
            mime == null ||
            !ALLOWED_TYPES.contains(mime.toLowerCase())
        ) {
            throw new BusinessException(
                "Invalid file type: "
                    + mime
                    + ". Allowed: JPEG, PNG, WebP, GIF"
            );
        }

        if (file.getSize() > MAX_SIZE) {

            throw new BusinessException(
                "File too large ("
                    + (file.getSize() / 1024 / 1024)
                    + " MB). Max 5 MB"
            );
        }
    }

    /**
     * Normalize Cloudinary folder.
     *
     * Example:
     *
     * "tcc/products/" → "tcc/products"
     * "/tcc/products/" → "tcc/products"
     * null/empty → "tcc/products"
     */
    private String normalizeFolder(String folder) {

        if (folder == null || folder.isBlank()) {
            return "tcc/products";
        }

        String normalized = folder.trim();

        // Remove leading slash
        normalized = normalized.replaceFirst("^/+", "");

        // Remove trailing slash
        normalized = normalized.replaceFirst("/+$", "");

        return normalized.isBlank()
            ? "tcc/products"
            : normalized;
    }

    /**
     * Upload image to Cloudinary.
     *
     * IMPORTANT:
     *
     * public_id contains ONLY the UUID.
     *
     * folder contains "tcc/products".
     *
     * This prevents:
     *
     * tcc/products/tcc/products/UUID
     *
     * and produces:
     *
     * tcc/products/UUID
     */
    private String doUpload(
        MultipartFile file,
        String folder
    ) {

        return doUploadWithPublicId(
            file,
            folder
        ).url();
    }

    /**
     * Internal upload method which returns both URL and public ID.
     */
    private UploadResult doUploadWithPublicId(
        MultipartFile file,
        String folder
    ) {

        try {

            /*
             * IMPORTANT:
             *
             * Do NOT include folder here.
             *
             * ❌ folder + "/" + UUID
             *
             * because "folder" is already passed separately.
             *
             * ✅ UUID only
             */
            String publicId =
                UUID.randomUUID().toString();

            /*
             * Resize only when needed.
             *
             * 1200x1200 maximum.
             * "limit" prevents upscaling.
             */
            Transformation transformation =
                new Transformation()
                    .width(1200)
                    .height(1200)
                    .crop("limit")
                    .quality("auto");

            Map<?, ?> result =
                cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(

                        // Example:
                        // public_id = UUID
                        "public_id",
                        publicId,

                        // Example:
                        // folder = tcc/products
                        "folder",
                        folder,

                        "overwrite",
                        true,

                        "resource_type",
                        "image",

                        "fetch_format",
                        "auto",

                        "transformation",
                        transformation
                    )
                );

            String url =
                (String) result.get("secure_url");

            if (url == null || url.isBlank()) {

                throw new BusinessException(
                    "Cloudinary did not return secure URL"
                );
            }

            /*
             * Cloudinary's actual public_id can be taken
             * directly from the response.
             */
            String cloudinaryPublicId =
                (String) result.get("public_id");

            if (
                cloudinaryPublicId == null ||
                cloudinaryPublicId.isBlank()
            ) {
                cloudinaryPublicId =
                    folder + "/" + publicId;
            }

            log.info(
                "Uploaded to Cloudinary: {} → {}",
                file.getOriginalFilename(),
                url
            );

            log.debug(
                "Cloudinary public_id: {}",
                cloudinaryPublicId
            );

            return new UploadResult(
                url,
                cloudinaryPublicId
            );

        } catch (IOException e) {

            log.error(
                "Cloudinary upload failed for {}: {}",
                file.getOriginalFilename(),
                e.getMessage(),
                e
            );

            throw new BusinessException(
                "Upload to Cloudinary failed: "
                    + e.getMessage()
            );
        }
    }

    /**
     * Legacy helper.
     *
     * Kept in case another service calls it.
     */
    private String extractPublicId(String url) {

        if (url == null || url.isBlank()) {
            return "";
        }

        String path =
            url.replaceFirst(
                ".*/upload/(?:v\\d+/)?",
                ""
            );

        int dot =
            path.lastIndexOf('.');

        return dot > 0
            ? path.substring(0, dot)
            : path;
    }

    /**
     * Quiet delete used during bulk rollback.
     */
    private void deleteImageQuietly(
        String publicId
    ) {
        try {
            deleteImage(publicId);
        } catch (Exception ignored) {
            log.debug(
                "Ignoring Cloudinary rollback delete failure for {}",
                publicId
            );
        }
    }

    /**
     * Internal immutable upload result.
     */
    private record UploadResult(
        String url,
        String publicId
    ) {}
    private static final long MAX_RAW_SIZE = 20 * 1024 * 1024; // 20 MB, adjust as needed

    public Map<String, String> uploadRawFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty())
            throw new BusinessException("File is empty or missing");
        if (!"application/pdf".equalsIgnoreCase(file.getContentType()))
            throw new BusinessException("Only PDF files are allowed");
        if (file.getSize() > MAX_RAW_SIZE)
            throw new BusinessException("File too large (" + (file.getSize() / 1024 / 1024) + " MB). Max 20 MB");

        String normalizedFolder = normalizeFolder(folder);
        String publicId = UUID.randomUUID().toString();

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "public_id", publicId,
                    "folder", normalizedFolder,
                    "resource_type", "raw",   // ← key difference from image upload
                    "overwrite", true
                )
            );
            String url = (String) result.get("secure_url");
            String pid = (String) result.get("public_id");
            if (url == null) throw new BusinessException("Cloudinary did not return secure URL");
            return Map.of("url", url, "publicId", pid != null ? pid : normalizedFolder + "/" + publicId);
        } catch (IOException e) {
            throw new BusinessException("PDF upload to Cloudinary failed: " + e.getMessage());
        }
    }
    /**
     * Upload an image and return BOTH url and publicId — unlike uploadImage(),
     * which only returns the URL. Needed anywhere the caller must later delete
     * or replace the image (e.g. gallery thumbnails).
     */
    public Map<String, String> uploadImageWithPublicId(MultipartFile file, String folder) {
        validateFile(file);
        String normalizedFolder = normalizeFolder(folder);
        UploadResult result = doUploadWithPublicId(file, normalizedFolder);
        return Map.of("url", result.url(), "publicId", result.publicId());
    }

    /**
     * Delete a raw file (PDF) from Cloudinary by public_id.
     * Non-fatal — logs warning on failure, mirrors deleteImage().
     */
    public void deleteRawFile(String publicId) {
        if (publicId == null || publicId.isBlank()) return;
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw"));
            log.info("Cloudinary raw file deleted: {}", publicId);
        } catch (IOException e) {
            log.warn("Could not delete Cloudinary raw file {}: {}", publicId, e.getMessage());
        }
    }
}