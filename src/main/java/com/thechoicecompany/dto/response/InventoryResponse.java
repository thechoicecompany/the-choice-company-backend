package com.thechoicecompany.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InventoryResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productSlug;
    private String productCategory;
    private String productImage;
    private Integer stockQty;
    private Integer reservedQty;
    private Integer availableQty;
    private Integer reorderLevel;
    private Integer maxStockQty;
    private Boolean isLowStock;
    private Boolean isOutOfStock;
    private String sku;
    private String warehouseNotes;
    private LocalDateTime lastRestockedAt;
    private LocalDateTime updatedAt;

    // Stock status label for UI badge
    public String getStockStatus() {
        if (availableQty == null || availableQty <= 0) return "OUT_OF_STOCK";
        if (isLowStock != null && isLowStock)          return "LOW_STOCK";
        return "IN_STOCK";
    }
}
