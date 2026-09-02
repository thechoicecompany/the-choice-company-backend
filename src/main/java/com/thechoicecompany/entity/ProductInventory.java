package com.thechoicecompany.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Tracks stock levels and movement for each product.
 * One-to-one relationship with Product.
 * stockQty     = current available stock units
 * reservedQty  = units reserved for pending orders (not yet dispatched)
 * reorderLevel = alert threshold — triggers low-stock notification
 * lastRestockedAt = when inventory was last topped up
 */
@Entity
@Table(name = "product_inventory")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    // Current available stock (physical units in warehouse)
    @Column(name = "stock_qty", nullable = false)
    @Builder.Default
    private Integer stockQty = 0;

    // Reserved for confirmed orders not yet dispatched
    @Column(name = "reserved_qty", nullable = false)
    @Builder.Default
    private Integer reservedQty = 0;

    // When stock drops to this number, trigger low-stock alert
    @Column(name = "reorder_level", nullable = false)
    @Builder.Default
    private Integer reorderLevel = 50;

    // Maximum capacity of this product in warehouse
    @Column(name = "max_stock_qty")
    @Builder.Default
    private Integer maxStockQty = 10000;

    // Last time stock was added/restocked
    @Column(name = "last_restocked_at")
    private LocalDateTime lastRestockedAt;

    // Internal SKU / barcode for warehouse tracking
    @Column(name = "sku", length = 100)
    private String sku;

    // Notes for warehouse team (storage location, special handling)
    @Column(name = "warehouse_notes", length = 500)
    private String warehouseNotes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Derived: actually available = stock - reserved
    @Transient
    public Integer getAvailableQty() {
        return Math.max(0, stockQty - reservedQty);
    }

    // Is stock running low?
    @Transient
    public Boolean isLowStock() {
        return getAvailableQty() <= reorderLevel;
    }
}
