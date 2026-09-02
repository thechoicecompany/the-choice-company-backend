package com.thechoicecompany.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Public-facing product response.
 * Used by Next.js frontend (product listing + detail pages).
 * Does NOT include admin-only fields (inventory details, audit).
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductResponse {
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
    private List<String> tags;
    private List<PricingTierDto> pricingTiers;
    private List<ProductResponse> relatedProducts;

    // Public-safe stock indicator — just IN_STOCK / OUT_OF_STOCK
    // (no actual numbers shown publicly)
    private String stockStatus;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PricingTierDto {
        private Integer minQty;
        private Integer maxQty;
        private BigDecimal price;
        private String label;
    }
}
