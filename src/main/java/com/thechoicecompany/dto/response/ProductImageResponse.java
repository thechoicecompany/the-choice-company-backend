package com.thechoicecompany.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Returned by the API after product creation.
 * Contains Cloudinary URLs + metadata for the frontend to render.
 *
 * Cloudinary URL transformations are applied via URL parameters:
 *   listing  → w_600,h_600,c_fill,q_auto,f_auto
 *   detail   → w_1000,h_1000,c_limit,q_auto,f_auto
 *   thumb    → w_150,h_150,c_fill,q_auto,f_auto
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductImageResponse {

    private Long   id;
    private Long   productId;

    /** Original Cloudinary URL — high quality */
    private String imageUrl;

    /** Cloudinary public_id — needed for deletion */
    private String publicId;

    /** 0-indexed display order */
    private Integer sortOrder;

    /** true = shown in product listings as primary image */
    private Boolean isPrimary;

    private LocalDateTime createdAt;

    // ── Derived Cloudinary transformation URLs ─────────────────────────────────
    // These are computed from imageUrl by replacing /upload/ with /upload/<transform>/
    // Done here so frontend doesn't need to know Cloudinary URL structure

    /** ~600×600 for product listing cards */
    public String getListingUrl() {
        return applyTransform(imageUrl, "w_600,h_600,c_fill,q_auto,f_auto");
    }

    /** ~1000×1000 for product detail page */
    public String getDetailUrl() {
        return applyTransform(imageUrl, "w_1000,h_1000,c_limit,q_auto,f_auto");
    }

    /** ~150×150 for thumbnails */
    public String getThumbnailUrl() {
        return applyTransform(imageUrl, "w_150,h_150,c_fill,q_auto,f_auto");
    }

    private static String applyTransform(String url, String transform) {
        if (url == null || !url.contains("/upload/")) return url;
        return url.replace("/upload/", "/upload/" + transform + "/");
    }
}
