package com.thechoicecompany.service;

import com.thechoicecompany.dto.request.CreateProductRequest.ProductImageInput;
import com.thechoicecompany.dto.response.ProductImageResponse;
import com.thechoicecompany.entity.Product;
import com.thechoicecompany.entity.ProductImage;
import com.thechoicecompany.exception.BusinessException;
import com.thechoicecompany.repository.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImageService {

    private final UploadService uploadService;
    private final ProductImageRepository productImageRepository;  // was commented out before

    private static final int MAX_IMAGES = 8;
    private static final long MAX_SIZE  = 5 * 1024 * 1024;
    private static final List<String> ALLOWED = List.of(
        "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    // ── PRIMARY FLOW: images pre-uploaded by frontend ─────────────────────────
    /**
     * Builds and saves ProductImage records from URLs already in Cloudinary.
     * Called by ProductService.createProduct() when productImages are present in request.
     * Must run inside the same @Transactional as product.save().
     */
    @Transactional
    public List<ProductImage> buildAndSaveImagesFromPayload(
            List<ProductImageInput> inputs, Product product) {

        if (inputs == null || inputs.isEmpty()) return List.of();
        if (inputs.size() > MAX_IMAGES)
            throw new BusinessException("Maximum " + MAX_IMAGES + " images allowed per product");

        List<ProductImage> images = new ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            ProductImageInput in = inputs.get(i);
            if (in.getUrl() == null || in.getUrl().isBlank())
                throw new BusinessException("Image " + (i + 1) + " is missing a URL");

            images.add(ProductImage.builder()
                .product(product)
                .imageUrl(in.getUrl())
                .publicId(in.getPublicId() != null ? in.getPublicId() : "")
                .sortOrder(in.getSortOrder())
                .isPrimary(in.getSortOrder() == 0)   // backend enforces: sort 0 = primary
                .build());
        }

        List<ProductImage> saved = productImageRepository.saveAll(images);
        log.info("Saved {} ProductImage records for product id={}", saved.size(), product.getId());
        return saved;
    }

    // ── SECONDARY FLOW: backend uploads files (multipart) ────────────────────
    /**
     * Validates files, uploads to Cloudinary, returns image records for DB save.
     * Used when the backend itself handles the upload (alternative flow).
     */
    @Transactional
    public List<ProductImage> uploadAndBuildImages(List<MultipartFile> files, Product product) {
        validateAll(files);

        List<Map<String, String>> uploaded = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                String url      = uploadService.uploadImage(file, "tcc/products");
                String publicId = extractPublicId(url);
                uploaded.add(Map.of("url", url, "publicId", publicId));
                log.info("Uploaded image {}/{}: {}", uploaded.size(), files.size(), url);
            }
        } catch (Exception ex) {
            log.error("Cloudinary upload failed after {}/{} images — rolling back",
                uploaded.size(), files.size());
            rollbackCloudinary(uploaded);
            throw new BusinessException("Image upload failed: " + ex.getMessage());
        }

        List<ProductImage> images = new ArrayList<>();
        for (int i = 0; i < uploaded.size(); i++) {
            images.add(ProductImage.builder()
                .product(product)
                .imageUrl(uploaded.get(i).get("url"))
                .publicId(uploaded.get(i).get("publicId"))
                .sortOrder(i)
                .isPrimary(i == 0)
                .build());
        }
        return productImageRepository.saveAll(images);
    }

    /** Deletes a single image from Cloudinary + removes from DB */
    @Transactional
    public void deleteImage(ProductImage image) {
        uploadService.deleteImage(image.getPublicId());
        productImageRepository.delete(image);
        log.info("Deleted product image: publicId={}", image.getPublicId());
    }
    /** Deletes an image by ID after verifying it belongs to the given product */
    @Transactional
    public void deleteImageById(Long productId, Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
            .orElseThrow(() -> new BusinessException("Image not found: id=" + imageId));

        if (!image.getProduct().getId().equals(productId)) {
            throw new BusinessException("Image does not belong to this product");
        }

        deleteImage(image); // existing method — Cloudinary destroy + DB delete
    }
    /** Cloudinary rollback on bulk failure */
    public void rollbackCloudinary(List<Map<String, String>> uploaded) {
        for (Map<String, String> entry : uploaded) {
            try {
                uploadService.deleteImage(entry.get("publicId"));
            } catch (Exception e) {
                log.error("Failed to rollback Cloudinary image {}: {}",
                    entry.get("publicId"), e.getMessage());
            }
        }
    }

    /** Maps a ProductImage entity → API response DTO */
    public ProductImageResponse toResponse(ProductImage img) {
        return ProductImageResponse.builder()
            .id(img.getId())
            .productId(img.getProduct().getId())
            .imageUrl(img.getImageUrl())
            .publicId(img.getPublicId())
            .sortOrder(img.getSortOrder())
            .isPrimary(img.getIsPrimary())
            .createdAt(img.getCreatedAt())
            .build();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void validateAll(List<MultipartFile> files) {
        if (files == null || files.isEmpty())
            throw new BusinessException("At least one product image is required");
        if (files.size() > MAX_IMAGES)
            throw new BusinessException("Maximum " + MAX_IMAGES + " images allowed per product");
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String ctx = "Image " + (i + 1);
            if (file.isEmpty()) throw new BusinessException(ctx + ": file is empty");
            if (file.getSize() > MAX_SIZE)
                throw new BusinessException(ctx + ": file exceeds 5 MB limit");
            String mime = file.getContentType();
            if (mime == null || !ALLOWED.contains(mime.toLowerCase()))
                throw new BusinessException(ctx + ": invalid type '" + mime + "'");
        }
    }

    private String extractPublicId(String url) {
        if (url == null) return "";
        String path = url.replaceAll(".*/upload/(v\\d+/)?", "");
        int dotIdx  = path.lastIndexOf('.');
        return dotIdx > 0 ? path.substring(0, dotIdx) : path;
    }
}