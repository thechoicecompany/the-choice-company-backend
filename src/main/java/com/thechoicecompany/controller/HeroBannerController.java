package com.thechoicecompany.controller;

import com.thechoicecompany.dto.response.HeroBannerDto;
import com.thechoicecompany.service.HeroBannerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HeroBannerController {

    private final HeroBannerService service;

    // ── Public ───────────────────────────────────────────────────────────────

    /**
     * GET /api/hero-banners
     *
     * Returns up to 3 active banners ordered by displayOrder.
     * No auth required — consumed by the public homepage.
     */
    @GetMapping("/api/hero-banners")
    public ResponseEntity<List<HeroBannerDto.Response>> getActiveBanners() {
        return ResponseEntity.ok(service.getActiveBanners());
    }

    // ── Admin ────────────────────────────────────────────────────────────────

    /**
     * GET /api/admin/hero-banners
     *
     * All banners (active + inactive), for the admin panel.
     */
    @GetMapping("/api/admin/hero-banners")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<List<HeroBannerDto.Response>> getAll() {
        return ResponseEntity.ok(service.getAllBanners());
    }

    /**
     * GET /api/admin/hero-banners/{id}
     */
    @GetMapping("/api/admin/hero-banners/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<HeroBannerDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * POST /api/admin/hero-banners
     *
     * Image must be uploaded first via POST /api/upload/image
     * (your existing UploadService). Pass the returned url + publicId here.
     */
    @PostMapping("/api/admin/hero-banners")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<HeroBannerDto.Response> create(
            @Valid @RequestBody HeroBannerDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    /**
     * PUT /api/admin/hero-banners/{id}
     *
     * Partial update — only pass fields you want to change.
     * To replace the image: include imageUrl + imagePublicId from a fresh /api/upload/image call.
     */
    @PutMapping("/api/admin/hero-banners/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<HeroBannerDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody HeroBannerDto.UpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }


    /**
     * PATCH /api/admin/hero-banners/{id}/toggle
     *
     * Flip active ↔ inactive without a full update.
     */
    @PatchMapping("/api/admin/hero-banners/{id}/toggle")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<HeroBannerDto.Response> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(service.toggleActive(id));
    }

    /**
     * POST /api/admin/hero-banners/reorder
     *
     * Body: { "orderedIds": [3, 1, 2] }
     * Index 0 becomes displayOrder 0 (first slide).
     */
    @PostMapping("/api/admin/hero-banners/reorder")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<List<HeroBannerDto.Response>> reorder(
            @Valid @RequestBody HeroBannerDto.ReorderRequest req) {
        return ResponseEntity.ok(service.reorder(req));
    }
    
    /**
     * DELETE /api/admin/hero-banners/{id}
     *
     * Deletes the banner and removes its image from Cloudinary.
     */
    @DeleteMapping("/api/admin/hero-banners/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','CONTENT_MANAGER')") // was hasRole('SUPER_ADMIN')
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    
}