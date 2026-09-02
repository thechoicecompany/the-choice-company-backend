package com.thechoicecompany.controller;

import com.thechoicecompany.dto.response.ApiResponse;
import com.thechoicecompany.dto.response.PagedResponse;
import com.thechoicecompany.dto.response.SampleProductResponse;
import com.thechoicecompany.service.SampleProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sample-products")
@RequiredArgsConstructor
@Tag(name = "Sample Products", description = "Public sample-shop catalogue (demo purchases)")
public class SampleProductController {

    private final SampleProductService service;

    @GetMapping
    @Operation(summary = "List active sample products, optionally filtered by category")
    public ResponseEntity<ApiResponse<PagedResponse<SampleProductResponse>>> list(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.listActive(category, page, size)));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get a single active sample product by slug")
    public ResponseEntity<ApiResponse<SampleProductResponse>> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(service.getBySlug(slug)));
    }

    @GetMapping("/slugs")
    @Operation(summary = "All active slugs — used by Next.js generateStaticParams")
    public ResponseEntity<ApiResponse<List<String>>> slugs() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllActiveSlugs()));
    }

    @GetMapping("/categories")
    @Operation(summary = "Distinct categories currently in use by active products")
    public ResponseEntity<ApiResponse<List<String>>> categories() {
        return ResponseEntity.ok(ApiResponse.success(service.getCategories()));
    }
}