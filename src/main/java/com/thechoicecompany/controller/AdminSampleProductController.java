package com.thechoicecompany.controller;

import com.thechoicecompany.dto.request.CreateSampleProductRequest;
import com.thechoicecompany.dto.request.UpdateSampleProductRequest;
import com.thechoicecompany.dto.response.ApiResponse;
import com.thechoicecompany.dto.response.PagedResponse;
import com.thechoicecompany.dto.response.SampleProductAdminResponse;
import com.thechoicecompany.service.SampleProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sample-products")
@RequiredArgsConstructor
@Tag(name = "Admin — Sample Products", description = "Admin CRUD for the sample-shop catalogue")
@SecurityRequirement(name = "bearerAuth")
public class AdminSampleProductController {

    private final SampleProductService service;

    @GetMapping
    @Operation(summary = "List all sample products, incl. inactive (admin)")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER','SALES_MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<SampleProductAdminResponse>>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.listAllAdmin(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single sample product (admin)")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER','SALES_MANAGER')")
    public ResponseEntity<ApiResponse<SampleProductAdminResponse>> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.getAdmin(id)));
    }

    @PostMapping
    @Operation(summary = "Create a new sample product")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<SampleProductAdminResponse>> create(
            @Valid @RequestBody CreateSampleProductRequest request) {
        SampleProductAdminResponse created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(created, "Sample product created"));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update sample product (PATCH — only provided fields updated)")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<SampleProductAdminResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSampleProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request), "Sample product updated"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete (sets isActive=false)")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> softDelete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Sample product deactivated"));
    }

    @DeleteMapping("/{id}/permanent")
    @Operation(summary = "Permanently delete — IRREVERSIBLE")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> hardDelete(@PathVariable Long id) {
        service.hardDelete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Sample product permanently deleted"));
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    @Operation(summary = "Delete a single sample product image")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable Long productId, @PathVariable Long imageId) {
        service.deleteImage(productId, imageId);
        return ResponseEntity.ok(ApiResponse.success(null, "Image deleted"));
    }
}