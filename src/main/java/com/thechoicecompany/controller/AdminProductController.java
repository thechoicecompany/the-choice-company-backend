package com.thechoicecompany.controller;

import com.thechoicecompany.dto.request.CreateProductRequest;
import com.thechoicecompany.dto.request.UpdateInventoryRequest;
import com.thechoicecompany.dto.request.UpdatePricingRequest;
import com.thechoicecompany.dto.request.UpdateProductRequest;
import com.thechoicecompany.dto.response.*;
import com.thechoicecompany.service.InventoryService;
import com.thechoicecompany.service.ProductImageService;
import com.thechoicecompany.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin — Products", description = "Admin product and inventory management")
@SecurityRequirement(name = "bearerAuth")
public class AdminProductController {

    private final ProductService   productService;
    private final InventoryService inventoryService;
    private final ProductImageService productImageService;

    // ── PRODUCT CRUD ──────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List all products with inventory info (admin)")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER','CONTENT_MANAGER','PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<ProductAdminResponse>>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
            productService.listProductsAdmin(page, size)
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single product with inventory (admin)")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER','CONTENT_MANAGER','PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<ProductAdminResponse>> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductAdmin(id)));
    }

    @PostMapping
    @Operation(summary = "Create new product — auto-creates inventory record")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER','CONTENT_MANAGER','PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<ProductAdminResponse>> create(
            @Valid @RequestBody CreateProductRequest request) {
        ProductAdminResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Product created successfully"));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update product details (PATCH — only provided fields updated)")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER','CONTENT_MANAGER','PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<ProductAdminResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            productService.updateProduct(id, request), "Product updated"
        ));
    }

    @PutMapping("/{id}/pricing")
    @Operation(summary = "Replace product base price + all pricing tiers")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER','CONTENT_MANAGER','PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<ProductAdminResponse>> updatePricing(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePricingRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            productService.updatePricing(id, request), "Pricing updated"
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete product (sets isActive=false — keeps data)")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER','CONTENT_MANAGER','PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> softDelete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deactivated"));
    }

    @DeleteMapping("/{id}/permanent")
    @Operation(summary = "Permanently delete product — IRREVERSIBLE")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> hardDelete(@PathVariable Long id) {
        productService.hardDeleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product permanently deleted"));
    }
    @DeleteMapping("/{productId}/images/{imageId}")
    @Operation(summary = "Delete a single product image — removes from Cloudinary + DB")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteProductImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        productImageService.deleteImageById(productId, imageId);
        return ResponseEntity.ok(ApiResponse.success(null, "Image deleted"));
    }

    // ── INVENTORY MANAGEMENT ──────────────────────────────────

    @GetMapping("/inventory")
    @Operation(summary = "Get full inventory table — all products with stock levels")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER','SALES_MANAGER')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getAllInventory() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getAllInventory()));
    }

    @GetMapping("/{id}/inventory")
    @Operation(summary = "Get inventory details for a specific product")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER','SALES_MANAGER')")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getInventoryByProduct(id)));
    }

    @PatchMapping("/{id}/inventory")
    @Operation(summary = "Update inventory — RESTOCK, ADJUSTMENT, RESERVED, DISPATCHED etc.")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInventoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            inventoryService.updateInventory(id, request), "Inventory updated"
        ));
    }

    @GetMapping("/inventory/low-stock")
    @Operation(summary = "Get all products with stock at or below reorder level")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER','SALES_MANAGER')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> lowStock() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getLowStockItems()));
    }

    @GetMapping("/inventory/out-of-stock")
    @Operation(summary = "Get all products with zero available stock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER','SALES_MANAGER')")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> outOfStock() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getOutOfStockItems()));
    }
}
