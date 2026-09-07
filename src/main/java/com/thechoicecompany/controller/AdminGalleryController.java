package com.thechoicecompany.controller;

import com.thechoicecompany.dto.request.GalleryItemUpdateRequest;
import com.thechoicecompany.dto.response.ApiResponse;
import com.thechoicecompany.dto.response.GalleryItemDto;
import com.thechoicecompany.service.AdminGalleryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/gallery")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER')")
@Tag(name = "Admin Gallery", description = "Gallery management — SUPER_ADMIN and CONTENT_MANAGER only")
public class AdminGalleryController {

    private final AdminGalleryService adminGalleryService;

    @GetMapping
    @Operation(summary = "List all gallery items including inactive")
    public ResponseEntity<ApiResponse<List<GalleryItemDto>>> list() {
        return ResponseEntity.ok(ApiResponse.success(adminGalleryService.listAll()));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a new gallery item — thumbnail image + catalogue PDF")
    public ResponseEntity<ApiResponse<GalleryItemDto>> create(
            @RequestPart("thumbnail")   MultipartFile thumbnail,
            @RequestPart("pdf")         MultipartFile pdf,
            @RequestPart("projectName") String projectName,
            @RequestPart("caption")     String caption,
            @RequestPart("category")    String category,
            @RequestPart(value = "clientIndustry", required = false) String clientIndustry,
            @RequestPart(value = "quantity",        required = false) String quantity,
            @RequestPart(value = "sortOrder",       required = false) String sortOrder) {
        return ResponseEntity.ok(ApiResponse.success(
            adminGalleryService.create(thumbnail, pdf, projectName, caption, category,
                clientIndustry, quantity, sortOrder)));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update metadata — no file re-upload")
    public ResponseEntity<ApiResponse<GalleryItemDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody GalleryItemUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success(adminGalleryService.update(id, req)));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle active/inactive visibility")
    public ResponseEntity<ApiResponse<GalleryItemDto>> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminGalleryService.toggleActive(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete item and its Cloudinary assets")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        adminGalleryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}