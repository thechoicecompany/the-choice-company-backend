//package com.thechoicecompany.dto.response;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//
///**
// * Extended product response for admin panel.
// * Includes inventory info, pricing tiers, and audit fields.
// * Public API uses ProductResponse (without admin fields).
// */
//@Data @Builder @NoArgsConstructor @AllArgsConstructor
//public class ProductAdminResponse {
//
//    // ── Core product fields ────────────────────────────────────
//    private Long id;
//    private String name;
//    private String slug;
//    private String category;
//    private String categorySlug;
//    private String description;
//    private String fullDescription;
//    private String image;
//    private List<String> images;
//    private Integer moq;
//    private BigDecimal basePrice;
//    private String material;
//    private String leadTime;
//    private List<String> brandingOptions;
//    private Boolean isFeatured;
//    private Boolean isActive;
//    private Integer sortOrder;
//    private List<String> tags;
//    private String metaTitle;
//    private String metaDescription;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//
//    // ── Pricing tiers ──────────────────────────────────────────
//    private List<PricingTierDto> pricingTiers;
//
//    // ── Inventory info (from product_inventory table) ──────────
//    private InventoryInfo inventory;
//
//    @Data @Builder @NoArgsConstructor @AllArgsConstructor
//    public static class PricingTierDto {
//        private Long id;
//        private Integer minQty;
//        private Integer maxQty;
//        private BigDecimal price;
//        private String label;
//        private Integer sortOrder;
//    }
//
//    @Data @Builder @NoArgsConstructor @AllArgsConstructor
//    public static class InventoryInfo {
//        private Long inventoryId;
//        private Integer stockQty;
//        private Integer reservedQty;
//        private Integer availableQty;
//        private Integer reorderLevel;
//        private Integer maxStockQty;
//        private Boolean isLowStock;
//        private String stockStatus;   // IN_STOCK | LOW_STOCK | OUT_OF_STOCK
//        private String sku;
//        private LocalDateTime lastRestockedAt;
//    }
//}

package com.thechoicecompany.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Extended product response for admin panel.
 * Includes inventory info, pricing tiers, product images, and audit fields.
 * Public API uses ProductResponse (without admin fields).
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductAdminResponse {

    // ── Core product fields ────────────────────────────────────
    private Long id;
    private String name;
    private String slug;
    private String category;
    private String categorySlug;
    private String description;
    private String fullDescription;
    private String image;
    private List<String> images;
    private Integer moq;
    private BigDecimal basePrice;
    private String material;
    private String leadTime;
    private List<String> brandingOptions;
    private Boolean isFeatured;
    private Boolean isActive;
    private Integer sortOrder;
    private List<String> tags;
    private String metaTitle;
    private String metaDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Pricing tiers ──────────────────────────────────────────
    private List<PricingTierDto> pricingTiers;

    // ── Product images (from product_images table) ─────────────
    // Ordered by sort_order ASC. First entry (isPrimary=true) matches `image` field above.
    private List<ProductImageResponse> productImages;

    // ── Inventory info (from product_inventory table) ──────────
    private InventoryInfo inventory;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PricingTierDto {
        private Long id;
        private Integer minQty;
        private Integer maxQty;
        private BigDecimal price;
        private String label;
        private Integer sortOrder;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class InventoryInfo {
        private Long inventoryId;
        private Integer stockQty;
        private Integer reservedQty;
        private Integer availableQty;
        private Integer reorderLevel;
        private Integer maxStockQty;
        private Boolean isLowStock;
        private String stockStatus;   // IN_STOCK | LOW_STOCK | OUT_OF_STOCK
        private String sku;
        private LocalDateTime lastRestockedAt;
    }
}