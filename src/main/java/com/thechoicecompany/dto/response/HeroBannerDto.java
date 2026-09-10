package com.thechoicecompany.dto.response;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

// ─── Response DTO ────────────────────────────────────────────────────────────

public class HeroBannerDto {

    @Data
    public static class Response {
        private Long id;
        private String tag;
        private String headlineTop;
        private String headlineBottom;
        private String body;
        private String imageUrl;
        private String imagePublicId;
        private Integer displayOrder;
        private Boolean active;
        private String ctaLink;
        private String ctaLabel;
    }

    // ─── Create request (multipart — image uploaded separately via /api/upload) ─

    @Data
    public static class CreateRequest {

        @NotBlank
        @Size(max = 120)
        private String tag;

        @NotBlank
        @Size(max = 200)
        private String headlineTop;

        @NotBlank
        @Size(max = 200)
        private String headlineBottom;

        @NotBlank
        @Size(max = 400)
        private String body;

        /** Cloudinary URL returned by the /api/upload endpoint */
        @NotBlank
        private String imageUrl;

        /** Cloudinary public_id returned by /api/upload */
        @NotBlank
        private String imagePublicId;

        @NotNull
        private Integer displayOrder;

        private Boolean active = true;

        @Size(max = 300)
        private String ctaLink;

        @Size(max = 80)
        private String ctaLabel;
    }

    // ─── Update request — all fields optional ────────────────────────────────

    @Data
    public static class UpdateRequest {

        @Size(max = 120)
        private String tag;

        @Size(max = 200)
        private String headlineTop;

        @Size(max = 200)
        private String headlineBottom;

        @Size(max = 400)
        private String body;

        /** Pass new imageUrl + imagePublicId if the image changed, else omit */
        private String imageUrl;
        private String imagePublicId;

        private Integer displayOrder;
        private Boolean active;

        @Size(max = 300)
        private String ctaLink;

        @Size(max = 80)
        private String ctaLabel;
    }

    // ─── Reorder request ─────────────────────────────────────────────────────

    @Data
    public static class ReorderRequest {
        /** Ordered list of banner IDs — index 0 gets displayOrder = 0 */
        @NotNull
        private java.util.List<Long> orderedIds;
    }
}