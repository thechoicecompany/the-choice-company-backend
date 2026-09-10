package com.thechoicecompany.service;

import com.thechoicecompany.dto.response.HeroBannerDto;
import com.thechoicecompany.entity.HeroBanner;
import com.thechoicecompany.exception.BusinessException;
import com.thechoicecompany.repository.HeroBannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HeroBannerService {

    private final HeroBannerRepository repo;
    private final UploadService uploadService;

    // ── Public ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<HeroBannerDto.Response> getActiveBanners() {
        return repo.findByActiveTrueOrderByDisplayOrderAsc()
                   .stream()
                   .limit(3)
                   .map(this::toResponse)
                   .toList();
    }

    // ── Admin ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<HeroBannerDto.Response> getAllBanners() {
        return repo.findAllByOrderByDisplayOrderAsc()
                   .stream()
                   .map(this::toResponse)
                   .toList();
    }

    @Transactional(readOnly = true)
    public HeroBannerDto.Response getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public HeroBannerDto.Response create(HeroBannerDto.CreateRequest req) {
        HeroBanner banner = HeroBanner.builder()
            .tag(req.getTag())
            .headlineTop(req.getHeadlineTop())
            .headlineBottom(req.getHeadlineBottom())
            .body(req.getBody())
            .imageUrl(req.getImageUrl())
            .imagePublicId(req.getImagePublicId())
            .displayOrder(req.getDisplayOrder())
            .active(req.getActive() != null ? req.getActive() : true)
            .ctaLink(req.getCtaLink())
            .ctaLabel(req.getCtaLabel())
            .build();

        HeroBanner saved = repo.save(banner);
        log.info("Hero banner created: id={}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public HeroBannerDto.Response update(Long id, HeroBannerDto.UpdateRequest req) {
        HeroBanner banner = findOrThrow(id);

        if (req.getTag()             != null) banner.setTag(req.getTag());
        if (req.getHeadlineTop()     != null) banner.setHeadlineTop(req.getHeadlineTop());
        if (req.getHeadlineBottom()  != null) banner.setHeadlineBottom(req.getHeadlineBottom());
        if (req.getBody()            != null) banner.setBody(req.getBody());
        if (req.getDisplayOrder()    != null) banner.setDisplayOrder(req.getDisplayOrder());
        if (req.getActive()          != null) banner.setActive(req.getActive());
        if (req.getCtaLink()         != null) banner.setCtaLink(req.getCtaLink());
        if (req.getCtaLabel()        != null) banner.setCtaLabel(req.getCtaLabel());

        // Image replacement: the DB write must succeed even if the old
        // Cloudinary asset can't be deleted (timeout, bad/placeholder id,
        // rate limit) — a failed cleanup call should never block a save.
        if (req.getImageUrl() != null && !req.getImageUrl().isBlank()) {
            safeDeleteImage(banner.getImagePublicId());
            banner.setImageUrl(req.getImageUrl());
            banner.setImagePublicId(req.getImagePublicId());
        }

        log.info("Hero banner updated: id={}", id);
        return toResponse(repo.save(banner));
    }

    @Transactional
    public void delete(Long id) {
        HeroBanner banner = findOrThrow(id);
        safeDeleteImage(banner.getImagePublicId());
        repo.delete(banner);
        log.info("Hero banner deleted: id={}", id);
    }

    /**
     * Reorder banners. Client sends the full ordered list of IDs;
     * index 0 → displayOrder 0, index 1 → displayOrder 1, …
     * Batched via findAllById/saveAll instead of one query per banner.
     */
    @Transactional
    public List<HeroBannerDto.Response> reorder(HeroBannerDto.ReorderRequest req) {
        List<Long> ids = req.getOrderedIds();

        List<HeroBanner> banners = repo.findAllById(ids);
        if (banners.size() != ids.size()) {
            throw new BusinessException("One or more banner IDs in reorder request do not exist.");
        }

        java.util.Map<Long, HeroBanner> byId = new java.util.HashMap<>();
        for (HeroBanner b : banners) byId.put(b.getId(), b);

        List<HeroBanner> toSave = new ArrayList<>(ids.size());
        for (int i = 0; i < ids.size(); i++) {
            HeroBanner b = byId.get(ids.get(i));
            b.setDisplayOrder(i);
            toSave.add(b);
        }
        repo.saveAll(toSave);

        log.info("Hero banners reordered: {}", ids);
        return getAllBanners();
    }

    @Transactional
    public HeroBannerDto.Response toggleActive(Long id) {
        HeroBanner banner = findOrThrow(id);
        banner.setActive(!banner.getActive());
        return toResponse(repo.save(banner));
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private HeroBanner findOrThrow(Long id) {
        return repo.findById(id)
                   .orElseThrow(() -> new BusinessException("Hero banner not found: " + id));
    }

    /**
     * Deletes a Cloudinary image if the publicId is present and not one of
     * the seed migration's placeholder values. Never throws — a failed
     * cleanup call must not block the DB write it's associated with.
     */
    private void safeDeleteImage(String publicId) {
        if (publicId == null || publicId.isBlank() || publicId.startsWith("placeholder/")) {
            return;
        }
        try {
            uploadService.deleteImage(publicId);
        } catch (Exception e) {
            log.warn("Failed to delete Cloudinary image (publicId={}) — continuing anyway: {}",
                     publicId, e.getMessage());
        }
    }

    private HeroBannerDto.Response toResponse(HeroBanner b) {
        HeroBannerDto.Response r = new HeroBannerDto.Response();
        r.setId(b.getId());
        r.setTag(b.getTag());
        r.setHeadlineTop(b.getHeadlineTop());
        r.setHeadlineBottom(b.getHeadlineBottom());
        r.setBody(b.getBody());
        r.setImageUrl(b.getImageUrl());
        r.setImagePublicId(b.getImagePublicId());
        r.setDisplayOrder(b.getDisplayOrder());
        r.setActive(b.getActive());
        r.setCtaLink(b.getCtaLink());
        r.setCtaLabel(b.getCtaLabel());
        return r;
    }
}