//package com.thechoicecompany.dto.request;
//
//import jakarta.validation.constraints.*;
//import lombok.Data;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//@Data
//public class CreateProductRequest {
//
//    @NotBlank @Size(min = 3, max = 200)
//    private String name;
//
//    // If null, auto-generated from name using SlugUtils
//    private String slug;
//
//    @NotBlank @Size(min = 2, max = 100)
//    private String category;
//
//    @NotBlank @Size(min = 2, max = 100)
//    private String categorySlug;
//
//    @NotBlank @Size(min = 10, max = 500)
//    private String description;
//
//    private String fullDescription;
//
//    @NotBlank
//    private String image;                          // Primary image URL
//
//    private List<String> images;                   // Gallery URLs
//
//    @NotNull @Min(1) @Max(100000)
//    private Integer moq;                           // Minimum order quantity
//
//    @NotNull @DecimalMin("1.0")
//    private BigDecimal basePrice;                  // Starting price per unit
//
//    private String material;
//    private String leadTime;
//
//    private List<String> brandingOptions;          // Screen Print, Embroidery etc.
//
//    private Boolean isFeatured = false;
//
//    private List<String> tags;
//
//    private String metaTitle;
//    private String metaDescription;
//
//    // Initial pricing tiers (optional — can add later)
//    private List<PricingTierInput> pricingTiers;
//
//    // Initial inventory (optional — auto-creates with 0 stock)
//    private InventoryInput inventory;
//
//    @Data
//    public static class PricingTierInput {
//        @NotNull @Min(1) private Integer minQty;
//        @NotNull @Min(1) private Integer maxQty;
//        @NotNull @DecimalMin("0.01") private BigDecimal price;
//        private String label;
//    }
//
//    @Data
//    public static class InventoryInput {
//        @Min(0) private Integer stockQty = 0;
//        @Min(0) private Integer reorderLevel = 50;
//        @Min(0) private Integer maxStockQty = 10000;
//        private String sku;
//        private String warehouseNotes;
//    }
//}


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
public class CreateProductRequest {

    @NotBlank @Size(min = 3, max = 200)
    private String name;

    private String slug;

    @NotBlank @Size(min = 2, max = 100)
    private String category;

    private String categorySlug;

    @NotBlank @Size(min = 10, max = 500)
    private String description;

    private String fullDescription;

    /**
     * Primary image URL — kept for backward compatibility with the products.image column.
     * When productImages is supplied, this is set automatically to the first image's URL.
     * Still @NotBlank so validation fails fast if the frontend sends neither.
     */
    @NotBlank
    private String image;

    /** Extra image URLs stored directly on the product entity (legacy list) */
    private List<String> images;

    @NotNull @Min(1) @Max(100000)
    private Integer moq;

    @NotNull @DecimalMin("1.0")
    private BigDecimal basePrice;

    private String material;
    private String leadTime;
    private List<String> brandingOptions;
    private Boolean isFeatured;
    private List<String> tags;
    private String metaTitle;
    private String metaDescription;

    private List<PricingTierInput> pricingTiers;

    private InventoryInput inventory;

    // ── NEW: pre-uploaded Cloudinary images ──────────────────────────────────
    /**
     * Images already uploaded to Cloudinary by the frontend.
     * Each entry carries the Cloudinary URL + publicId + sort order.
     * ProductService maps these to ProductImage entities and persists them
     * in the same transaction as the Product itself.
     */
    @Valid
    private List<ProductImageInput> productImages;

    // ─────────────────────────────────────────────────────────────────────────

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PricingTierInput {
        private Integer minQty;
        private Integer maxQty;
        private BigDecimal price;
        private String label;
        private Integer sortOrder;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class InventoryInput {
        private Integer stockQty;
        private Integer reorderLevel;
        private Integer maxStockQty;
        private String sku;
        private String warehouseNotes;
    }

    // ── NEW nested DTO ───────────────────────────────────────────────────────
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProductImageInput {
        @NotBlank
        private String url;
        private String publicId;
        private int sortOrder;
        private boolean isPrimary;
    }
}
