package com.thechoicecompany.dto.request;

import com.thechoicecompany.enums.InventoryAction;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateInventoryRequest {

    @NotNull
    private InventoryAction action;

    // Units to add/remove/adjust
    @NotNull @Min(0)
    private Integer quantity;

    // Optional note explaining the action (e.g. "Received stock from supplier")
    @Size(max = 500)
    private String note;

    // For ADJUSTMENT action — set absolute stock level directly
    private Integer absoluteStock;

    // Update reorder alert threshold
    @Min(0)
    private Integer reorderLevel;

    // Update max warehouse capacity
    @Min(0)
    private Integer maxStockQty;

    private String sku;
    private String warehouseNotes;
}
