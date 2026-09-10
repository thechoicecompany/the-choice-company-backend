package com.thechoicecompany.controller;

import com.thechoicecompany.dto.response.ApiResponse;
import com.thechoicecompany.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Tag(name = "Upload", description = "Image upload to Cloudinary")
@SecurityRequirement(name = "bearerAuth")
public class UploadController {

    private final UploadService uploadService;

    /** Single image upload — used by gallery, logos etc. */
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload single image to Cloudinary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "tcc/products") String folder) {

        String url      = uploadService.uploadImage(file, folder);
        String publicId = url.replaceAll(".*/upload/(v\\d+/)?", "")
                             .replaceAll("\\.[a-zA-Z]+$", "");
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("url", url, "publicId", publicId),
                "Image uploaded"
        ));
    }

    /**
     * Single image upload — returns BOTH url AND publicId from Cloudinary directly.
     *
     * Used by: Hero Banner admin (needs publicId to delete/replace the image later).
     *
     * Difference from /image: this calls uploadImageWithPublicId() which gets the
     * real publicId from Cloudinary's response instead of reverse-engineering it
     * from the URL.
     */
    @PostMapping(value = "/image-with-id", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload single image, returns url + publicId from Cloudinary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadImageWithId(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "tcc/products") String folder) {

        Map<String, String> result = uploadService.uploadImageWithPublicId(file, folder);
        return ResponseEntity.ok(ApiResponse.success(result, "Image uploaded"));
    }

    /**
     * Bulk image upload — used by Add Product form.
     * Accepts up to 8 files in one request.
     * All-or-nothing: if one fails, all already-uploaded images are deleted.
     *
     * Returns: [ { url, publicId }, ... ] in same order as input files.
     */
    @PostMapping(value = "/images/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload multiple product images to Cloudinary (max 8)")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> uploadBulk(
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam(defaultValue = "tcc/products") String folder) {

        if (files == null || files.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<List<Map<String, String>>>success(null, "No files provided"));
        }
        if (files.size() > 8) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<List<Map<String, String>>>success(null, "Maximum 8 images per product"));
        }

        List<Map<String, String>> results = uploadService.uploadImages(files, folder);
        return ResponseEntity.ok(ApiResponse.success(
                results, files.size() + " image(s) uploaded successfully"
        ));
    }

    /** Gallery image upload */
    @PostMapping(value = "/gallery", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload gallery image to Cloudinary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadGallery(
            @RequestPart("file") MultipartFile file) {

        String url = uploadService.uploadImage(file, "tcc/gallery");
        return ResponseEntity.ok(ApiResponse.success(Map.of("url", url), "Gallery image uploaded"));
    }
}