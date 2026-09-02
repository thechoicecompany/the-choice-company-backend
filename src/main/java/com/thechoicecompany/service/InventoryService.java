package com.thechoicecompany.service;

import com.thechoicecompany.dto.request.UpdateInventoryRequest;
import com.thechoicecompany.dto.response.InventoryResponse;
import com.thechoicecompany.entity.Product;
import com.thechoicecompany.entity.ProductInventory;
import com.thechoicecompany.enums.InventoryAction;
import com.thechoicecompany.exception.BusinessException;
import com.thechoicecompany.exception.ResourceNotFoundException;
import com.thechoicecompany.repository.InventoryRepository;
import com.thechoicecompany.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository   productRepository;

    // ── Get all inventory (admin table) ───────────────────────
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAllWithProduct(true)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Get inventory for one product ─────────────────────────
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProduct(Long productId) {
        ProductInventory inv = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));
        return toResponse(inv);
    }

    // ── Get low-stock items ────────────────────────────────────
    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStockItems() {
        return inventoryRepository.findLowStockItems()
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Get out-of-stock items ─────────────────────────────────
    @Transactional(readOnly = true)
    public List<InventoryResponse> getOutOfStockItems() {
        return inventoryRepository.findOutOfStockItems()
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Update inventory by action ────────────────────────────
    @Transactional
    public InventoryResponse updateInventory(Long productId, UpdateInventoryRequest req) {
        ProductInventory inv = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory", "productId", productId));

        applyAction(inv, req);

        // Update optional config fields if provided
        if (req.getReorderLevel() != null) inv.setReorderLevel(req.getReorderLevel());
        if (req.getMaxStockQty()  != null) inv.setMaxStockQty(req.getMaxStockQty());
        if (req.getSku()          != null) inv.setSku(req.getSku());
        if (req.getWarehouseNotes()!= null)inv.setWarehouseNotes(req.getWarehouseNotes());

        ProductInventory saved = inventoryRepository.save(inv);
        log.info("Inventory updated for product {}: action={}, qty={}", productId, req.getAction(), req.getQuantity());
        return toResponse(saved);
    }

    // ── Auto-create inventory record when product is created ──
    @Transactional
    public ProductInventory createInventoryForProduct(Product product, Integer initialStock,
                                                       Integer reorderLevel, Integer maxStock,
                                                       String sku, String notes) {
        if (inventoryRepository.existsByProductId(product.getId())) {
            throw new BusinessException("Inventory already exists for product: " + product.getSlug());
        }
        ProductInventory inv = ProductInventory.builder()
            .product(product)
            .stockQty(initialStock != null ? initialStock : 0)
            .reservedQty(0)
            .reorderLevel(reorderLevel != null ? reorderLevel : 50)
            .maxStockQty(maxStock != null ? maxStock : 10000)
            .sku(sku)
            .warehouseNotes(notes)
            .lastRestockedAt(initialStock != null && initialStock > 0 ? LocalDateTime.now() : null)
            .build();
        return inventoryRepository.save(inv);
    }

    // ── Apply business logic for each action type ─────────────
    private void applyAction(ProductInventory inv, UpdateInventoryRequest req) {
        InventoryAction action = req.getAction();
        int qty = req.getQuantity() != null ? req.getQuantity() : 0;

        switch (action) {
            case RESTOCK -> {
                inv.setStockQty(inv.getStockQty() + qty);
                inv.setLastRestockedAt(LocalDateTime.now());
            }
            case ADJUSTMENT -> {
                // Set stock to absolute value
                int absolute = req.getAbsoluteStock() != null ? req.getAbsoluteStock() : qty;
                if (absolute < 0) throw new BusinessException("Stock quantity cannot be negative");
                inv.setStockQty(absolute);
            }
            case RESERVED -> {
                if (inv.getAvailableQty() < qty)
                    throw new BusinessException("Cannot reserve " + qty + " units — only " + inv.getAvailableQty() + " available");
                inv.setReservedQty(inv.getReservedQty() + qty);
            }
            case RELEASED -> {
                int newReserved = inv.getReservedQty() - qty;
                if (newReserved < 0) throw new BusinessException("Cannot release more than reserved quantity");
                inv.setReservedQty(newReserved);
            }
            case DISPATCHED -> {
                if (inv.getStockQty() < qty)
                    throw new BusinessException("Cannot dispatch " + qty + " units — only " + inv.getStockQty() + " in stock");
                inv.setStockQty(inv.getStockQty() - qty);
                int newReserved = Math.max(0, inv.getReservedQty() - qty);
                inv.setReservedQty(newReserved);
            }
            case DAMAGED, RETURNED -> {
                if (action == InventoryAction.DAMAGED) {
                    inv.setStockQty(Math.max(0, inv.getStockQty() - qty));
                } else {
                    inv.setStockQty(inv.getStockQty() + qty);
                }
            }
        }
    }

    // ── Mapper ────────────────────────────────────────────────
    public InventoryResponse toResponse(ProductInventory i) {
        int available = i.getAvailableQty();
        boolean lowStock = i.isLowStock();
        return InventoryResponse.builder()
            .id(i.getId())
            .productId(i.getProduct().getId())
            .productName(i.getProduct().getName())
            .productSlug(i.getProduct().getSlug())
            .productCategory(i.getProduct().getCategory())
            .productImage(i.getProduct().getImage())
            .stockQty(i.getStockQty())
            .reservedQty(i.getReservedQty())
            .availableQty(available)
            .reorderLevel(i.getReorderLevel())
            .maxStockQty(i.getMaxStockQty())
            .isLowStock(lowStock)
            .isOutOfStock(available <= 0)
            .sku(i.getSku())
            .warehouseNotes(i.getWarehouseNotes())
            .lastRestockedAt(i.getLastRestockedAt())
            .updatedAt(i.getUpdatedAt())
            .build();
    }
}
