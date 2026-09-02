package com.thechoicecompany.service;

import com.thechoicecompany.dto.request.CreateSampleProductRequest;
import com.thechoicecompany.dto.request.UpdateSampleProductRequest;
import com.thechoicecompany.dto.response.PagedResponse;
import com.thechoicecompany.dto.response.SampleProductAdminResponse;
import com.thechoicecompany.dto.response.SampleProductImageResponse;
import com.thechoicecompany.dto.response.SampleProductResponse;
import com.thechoicecompany.entity.SampleProduct;
import com.thechoicecompany.entity.SampleProductImage;
import com.thechoicecompany.exception.DuplicateResourceException;
import com.thechoicecompany.exception.ResourceNotFoundException;
import com.thechoicecompany.repository.SampleProductImageRepository;
import com.thechoicecompany.repository.SampleProductRepository;
import com.thechoicecompany.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SampleProductService {

    private final SampleProductRepository      repository;
    private final SampleProductImageRepository imageRepository;
    private final UploadService                uploadService; // reused Cloudinary service

    // ═══════════════════════════════════════════════════════════
    // PUBLIC API (storefront)
    // ═══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public PagedResponse<SampleProductResponse> listActive(String category, int page, int size) {
        Sort sorting = Sort.by("sortOrder").ascending().and(Sort.by("createdAt").descending());
        Page<SampleProduct> products = repository.findActiveWithFilters(
            category, PageRequest.of(page, size, sorting));
        return PagedResponse.from(products.map(this::toPublicResponse));
    }

    @Transactional(readOnly = true)
    public SampleProductResponse getBySlug(String slug) {
        SampleProduct product = repository.findBySlugAndIsActiveTrue(slug)
            .orElseThrow(() -> new ResourceNotFoundException("SampleProduct", "slug", slug));
        return toPublicResponse(product);
    }

    @Transactional(readOnly = true)
    public List<String> getAllActiveSlugs() {
        return repository.findAllActiveSlugs();
    }

    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return repository.findDistinctActiveCategories();
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN METHODS
    // ═══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public PagedResponse<SampleProductAdminResponse> listAllAdmin(int page, int size) {
        Page<SampleProduct> products = repository.findAll(
            PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PagedResponse.from(products.map(this::toAdminResponse));
    }

    @Transactional(readOnly = true)
    public SampleProductAdminResponse getAdmin(Long id) {
        return toAdminResponse(findOrThrow(id));
    }

    @Transactional
    public SampleProductAdminResponse create(CreateSampleProductRequest req) {
        String slug = (req.getSlug() != null && !req.getSlug().isBlank())
            ? SlugUtils.toSlug(req.getSlug())
            : SlugUtils.toSlug(req.getName());

        if (repository.existsBySlug(slug)) {
            throw new DuplicateResourceException("Sample product with slug '" + slug + "' already exists");
        }

        // Derive primary image + gallery from productImages if supplied, else fall back to legacy fields
        String primaryImageUrl = req.getImage();
        List<String> galleryUrls = new ArrayList<>();
        if (req.getProductImages() != null && !req.getProductImages().isEmpty()) {
            primaryImageUrl = req.getProductImages().get(0).getUrl();
            req.getProductImages().stream().skip(1).forEach(pi -> galleryUrls.add(pi.getUrl()));
        } else if (req.getImages() != null) {
            galleryUrls.addAll(req.getImages());
        }

        SampleProduct product = SampleProduct.builder()
            .name(req.getName())
            .slug(slug)
            .category(req.getCategory())
            .categorySlug(req.getCategorySlug())
            .description(req.getDescription())
            .image(primaryImageUrl)
            .images(galleryUrls)
            .samplePrice(req.getSamplePrice())
            .bulkPrice(req.getBulkPrice())
            .maxSampleQty(req.getMaxSampleQty())
            .moq(req.getMoq())
            .shippingDays(req.getShippingDays())
            .material(req.getMaterial())
            .dimensions(req.getDimensions())
            .weight(req.getWeight())
            .brandingOptions(req.getBrandingOptions() != null ? req.getBrandingOptions() : new ArrayList<>())
            .tags(req.getTags() != null ? req.getTags() : new ArrayList<>())
            .isActive(req.getIsActive() != null ? req.getIsActive() : true)
            .isBestseller(req.getIsBestseller() != null ? req.getIsBestseller() : false)
            .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0)
            .metaTitle(req.getMetaTitle())
            .metaDescription(req.getMetaDescription())
            .build();

        SampleProduct saved = repository.save(product);

        if (req.getProductImages() != null && !req.getProductImages().isEmpty()) {
            saveImages(saved, req.getProductImages());
        }

        log.info("Sample product created: {} ({})", saved.getName(), saved.getSlug());
        return toAdminResponse(saved);
    }

    @Transactional
    public SampleProductAdminResponse update(Long id, UpdateSampleProductRequest req) {
        SampleProduct product = findOrThrow(id);

        if (req.getName()            != null) product.setName(req.getName());
        if (req.getCategory()        != null) product.setCategory(req.getCategory());
        if (req.getCategorySlug()    != null) product.setCategorySlug(req.getCategorySlug());
        if (req.getDescription()     != null) product.setDescription(req.getDescription());
        if (req.getImage()           != null) product.setImage(req.getImage());
        if (req.getImages()          != null) product.setImages(req.getImages());
        if (req.getSamplePrice()     != null) product.setSamplePrice(req.getSamplePrice());
        if (req.getBulkPrice()       != null) product.setBulkPrice(req.getBulkPrice());
        if (req.getMaxSampleQty()    != null) product.setMaxSampleQty(req.getMaxSampleQty());
        if (req.getMoq()             != null) product.setMoq(req.getMoq());
        if (req.getShippingDays()    != null) product.setShippingDays(req.getShippingDays());
        if (req.getMaterial()        != null) product.setMaterial(req.getMaterial());
        if (req.getDimensions()      != null) product.setDimensions(req.getDimensions());
        if (req.getWeight()          != null) product.setWeight(req.getWeight());
        if (req.getBrandingOptions() != null) product.setBrandingOptions(req.getBrandingOptions());
        if (req.getTags()            != null) product.setTags(req.getTags());
        if (req.getIsActive()        != null) product.setIsActive(req.getIsActive());
        if (req.getIsBestseller()    != null) product.setIsBestseller(req.getIsBestseller());
        if (req.getSortOrder()       != null) product.setSortOrder(req.getSortOrder());
        if (req.getMetaTitle()       != null) product.setMetaTitle(req.getMetaTitle());
        if (req.getMetaDescription() != null) product.setMetaDescription(req.getMetaDescription());

        SampleProduct saved = repository.save(product);

        // Append any newly-uploaded images (existing images are removed individually via
        // the dedicated delete-image endpoint, not by replacing the whole list here).
        if (req.getProductImages() != null && !req.getProductImages().isEmpty()) {
            int existingCount = imageRepository.findBySampleProductIdOrderBySortOrderAsc(saved.getId()).size();
            List<CreateSampleProductRequest.ProductImageInput> offsetImages = new ArrayList<>();
            for (int i = 0; i < req.getProductImages().size(); i++) {
                CreateSampleProductRequest.ProductImageInput src = req.getProductImages().get(i);
                offsetImages.add(CreateSampleProductRequest.ProductImageInput.builder()
                    .url(src.getUrl())
                    .publicId(src.getPublicId())
                    .sortOrder(existingCount + i)
                    .isPrimary(existingCount == 0 && i == 0)
                    .build());
            }
            saveImages(saved, offsetImages);
        }

        log.info("Sample product updated: {}", saved.getSlug());
        return toAdminResponse(saved);
    }

    @Transactional
    public void softDelete(Long id) {
        SampleProduct product = findOrThrow(id);
        product.setIsActive(false);
        repository.save(product);
        log.info("Sample product deactivated: {}", product.getSlug());
    }

    @Transactional
    public void hardDelete(Long id) {
        SampleProduct product = findOrThrow(id);
        // Clean up Cloudinary assets first — non-fatal if any fail (see UploadService)
        imageRepository.findBySampleProductIdOrderBySortOrderAsc(id)
            .forEach(img -> {
                if (img.getPublicId() != null && !img.getPublicId().isBlank()) {
                    uploadService.deleteImage(img.getPublicId());
                }
            });
        imageRepository.deleteBySampleProductId(id);
        repository.delete(product);
        log.info("Sample product permanently deleted: id={}", id);
    }

    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        SampleProductImage image = imageRepository.findByIdAndSampleProductId(imageId, productId)
            .orElseThrow(() -> new ResourceNotFoundException("SampleProductImage", "id", imageId));

        if (image.getPublicId() != null && !image.getPublicId().isBlank()) {
            uploadService.deleteImage(image.getPublicId());
        }
        imageRepository.delete(image);

        // If we just removed the primary image, promote the next one and
        // keep the legacy `image` column on SampleProduct in sync.
        List<SampleProductImage> remaining =
            imageRepository.findBySampleProductIdOrderBySortOrderAsc(productId);
        SampleProduct product = findOrThrow(productId);
        if (!remaining.isEmpty()) {
            product.setImage(remaining.get(0).getImageUrl());
            List<String> gallery = remaining.stream().skip(1)
                .map(SampleProductImage::getImageUrl).collect(Collectors.toList());
            product.setImages(gallery);
            repository.save(product);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════

    private SampleProduct findOrThrow(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("SampleProduct", "id", id));
    }

    private void saveImages(SampleProduct product, List<CreateSampleProductRequest.ProductImageInput> inputs) {
        List<SampleProductImage> entities = inputs.stream()
            .map(pi -> SampleProductImage.builder()
                .sampleProduct(product)
                .imageUrl(pi.getUrl())
                .publicId(pi.getPublicId())
                .sortOrder(pi.getSortOrder())
                .isPrimary(pi.isPrimary())
                .build())
            .collect(Collectors.toList());
        imageRepository.saveAll(entities);
    }

    private SampleProductResponse toPublicResponse(SampleProduct p) {
        List<SampleProductImage> imgs = imageRepository.findBySampleProductIdOrderBySortOrderAsc(p.getId());
        List<String> gallery = !imgs.isEmpty()
            ? imgs.stream().map(SampleProductImage::getImageUrl).collect(Collectors.toList())
            : buildLegacyGallery(p);

        return SampleProductResponse.builder()
            .id(p.getId()).name(p.getName()).slug(p.getSlug())
            .category(p.getCategory()).description(p.getDescription())
            .image(p.getImage()).images(gallery)
            .samplePrice(p.getSamplePrice()).bulkPrice(p.getBulkPrice())
            .maxSampleQty(p.getMaxSampleQty()).moq(p.getMoq())
            .shippingDays(p.getShippingDays())
            .material(p.getMaterial()).dimensions(p.getDimensions()).weight(p.getWeight())
            .brandingOptions(p.getBrandingOptions()).tags(p.getTags())
            .build();
    }

    private SampleProductAdminResponse toAdminResponse(SampleProduct p) {
        List<SampleProductImageResponse> productImages =
            imageRepository.findBySampleProductIdOrderBySortOrderAsc(p.getId())
                .stream()
                .map(img -> SampleProductImageResponse.builder()
                    .id(img.getId()).imageUrl(img.getImageUrl()).publicId(img.getPublicId())
                    .sortOrder(img.getSortOrder()).isPrimary(img.getIsPrimary())
                    .build())
                .collect(Collectors.toList());

        return SampleProductAdminResponse.builder()
            .id(p.getId()).name(p.getName()).slug(p.getSlug())
            .category(p.getCategory()).categorySlug(p.getCategorySlug())
            .description(p.getDescription())
            .image(p.getImage()).images(p.getImages())
            .samplePrice(p.getSamplePrice()).bulkPrice(p.getBulkPrice())
            .maxSampleQty(p.getMaxSampleQty()).moq(p.getMoq())
            .shippingDays(p.getShippingDays())
            .material(p.getMaterial()).dimensions(p.getDimensions()).weight(p.getWeight())
            .brandingOptions(p.getBrandingOptions()).tags(p.getTags())
            .isActive(p.getIsActive()).isBestseller(p.getIsBestseller())
            .sortOrder(p.getSortOrder())
            .metaTitle(p.getMetaTitle()).metaDescription(p.getMetaDescription())
            .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt())
            .productImages(productImages)
            .build();
    }

    // Fallback for products created before the SampleProductImage table was used
    private List<String> buildLegacyGallery(SampleProduct p) {
        List<String> gallery = new ArrayList<>();
        gallery.add(p.getImage());
        if (p.getImages() != null) gallery.addAll(p.getImages());
        return gallery;
    }
}