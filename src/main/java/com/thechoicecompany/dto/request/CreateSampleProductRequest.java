package com.thechoicecompany.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSampleProductRequest {

    @NotBlank @Size(min = 3, max = 200)
    private String name;

    // If null/blank, auto-generated from name
    private String slug;

    @NotBlank @Size(min = 2, max = 100)
    private String category;

    private String categorySlug;

    @NotBlank @Size(min = 10, max = 500)
    private String description;

    /** Legacy primary image URL — auto-derived from productImages[0] if that's supplied. */
    @NotBlank
    private String image;

    /** Legacy extra gallery URLs (used only if productImages is not supplied). */
    private List<String> images;

    @NotNull @DecimalMin(value = "1.0", message = "Sample price must be at least ₹1")
    private BigDecimal samplePrice;

    @NotNull @DecimalMin(value = "1.0", message = "Bulk price must be at least ₹1")
    private BigDecimal bulkPrice;

    @NotNull @Min(1) @Max(20)
    private Integer maxSampleQty;

    @NotNull @Min(1) @Max(100000)
    private Integer moq;

    @NotNull @Min(1) @Max(60)
    private Integer shippingDays;

    private String material;
    private String dimensions;
    private String weight;

    private List<String> brandingOptions;
    private List<String> tags;

    private Boolean isBestseller;
    private Boolean isActive;
    private Integer sortOrder;

    private String metaTitle;
    private String metaDescription;

    /** Pre-uploaded Cloudinary images from the frontend uploader (primary flow). */
    @Valid
    private List<ProductImageInput> productImages;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProductImageInput {
        @NotBlank
        private String url;
        private String publicId;
        private int sortOrder;
        private boolean isPrimary;
    }
}