package com.thechoicecompany.controller;

import com.thechoicecompany.dto.response.ApiResponse;
import com.thechoicecompany.dto.response.PagedResponse;
import com.thechoicecompany.dto.response.ProductResponse;
import com.thechoicecompany.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public product endpoints — no authentication required.
 * Called by Next.js frontend for product listing, detail, and sitemap.
 * Admin product management → AdminProductController (/api/admin/products)
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products — Public", description = "Public product catalogue endpoints")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/slugs")
    @Operation(summary = "All active slugs — for Next.js generateStaticParams()")
    public ResponseEntity<ApiResponse<List<String>>> slugs() {
        return ResponseEntity.ok(ApiResponse.success(productService.getAllSlugs()));
    }

    @GetMapping("/categories")
    @Operation(summary = "All distinct categories for filter UI")
    public ResponseEntity<ApiResponse<List<String>>> categories() {
        return ResponseEntity.ok(ApiResponse.success(productService.getCategories()));
    }

    @GetMapping("/featured")
    @Operation(summary = "Featured products for home page")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> featured(
            @RequestParam(defaultValue = "12") int limit) {
        return ResponseEntity.ok(ApiResponse.success(productService.getFeaturedProducts(limit)));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Full product detail by slug")
    public ResponseEntity<ApiResponse<ProductResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductBySlug(slug)));
    }
    @GetMapping
    @Operation(summary = "List active products with optional filters")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) String occasion,
            @RequestParam(required = false) String budget,
            @RequestParam(required = false) Integer moq,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "24") int size,
            @RequestParam(defaultValue = "popular") String sort) {
        return ResponseEntity.ok(ApiResponse.success(
            productService.listProducts(category, featured, occasion, budget, moq, page, size, sort)
        ));
    }
}

