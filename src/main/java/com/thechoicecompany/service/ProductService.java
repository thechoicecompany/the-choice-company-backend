package com.thechoicecompany.service;

import com.thechoicecompany.dto.request.CreateProductRequest;
import com.thechoicecompany.dto.request.UpdatePricingRequest;
import com.thechoicecompany.dto.request.UpdateProductRequest;
import com.thechoicecompany.dto.response.PagedResponse;
import com.thechoicecompany.dto.response.ProductAdminResponse;
import com.thechoicecompany.dto.response.ProductImageResponse;
import com.thechoicecompany.dto.response.ProductResponse;
import com.thechoicecompany.entity.Product;
import com.thechoicecompany.entity.ProductImage;
import com.thechoicecompany.entity.ProductInventory;
import com.thechoicecompany.entity.ProductPricingTier;
import com.thechoicecompany.exception.DuplicateResourceException;
import com.thechoicecompany.exception.ResourceNotFoundException;
import com.thechoicecompany.repository.InventoryRepository;
import com.thechoicecompany.repository.ProductImageRepository;
import com.thechoicecompany.repository.ProductRepository;
import com.thechoicecompany.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final UploadService          uploadService;
    private final ProductRepository      productRepository;
    private final InventoryRepository    inventoryRepository;
    private final InventoryService       inventoryService;
    private final ProductImageService    productImageService;
    private final ProductImageRepository productImageRepository;

    // ═══════════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> listProducts(
            String category, Boolean featured,
            String occasion, String budget, Integer moq,
            int page, int size, String sort) {

        Sort sorting = switch (sort != null ? sort : "popular") {
            case "price-asc"  -> Sort.by("base_price").ascending();
            case "price-desc" -> Sort.by("base_price").descending();
            case "newest"     -> Sort.by("created_at").descending();
            default           -> Sort.by("sort_order").ascending()
                                     .and(Sort.by("is_featured").descending());
        };

        BigDecimal minPrice = null, maxPrice = null;
        if (budget != null && !budget.isBlank()) {
            if (budget.endsWith("+")) {
                minPrice = new BigDecimal(budget.replace("+", ""));
            } else {
                String[] parts = budget.split("-");
                if (parts.length == 2) {
                    minPrice = new BigDecimal(parts[0]);
                    maxPrice = new BigDecimal(parts[1]);
                }
            }
        }

        Page<Product> products = productRepository.findWithFilters(
                category, featured, occasion, minPrice, maxPrice, moq,
                PageRequest.of(page, size, sorting));

        // Force-initialize pricingTiers (native query can't use @EntityGraph).
        // With hibernate.default_batch_fetch_size=20 this becomes 1 batched query,
        // not N queries. Confirm that property is set in application.properties.
        products.forEach(p -> p.getPricingTiers().size());

        // ── Batch-load inventory for the whole page in one query ──────────────
        List<Long> ids = products.map(Product::getId).getContent();
        Map<Long, ProductInventory> inventoryByProductId = inventoryRepository
                .findAllByProductIdIn(ids)
                .stream()
                .collect(Collectors.toMap(i -> i.getProduct().getId(), i -> i));

        return PagedResponse.from(
                products.map(p -> toPublicResponse(p, inventoryByProductId.get(p.getId())))
        );
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlugAndIsActiveTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
        // Single product — single inventory query is fine here
        ProductInventory inv = inventoryRepository.findByProductId(product.getId()).orElse(null);
        return toPublicResponse(product, inv);
    }

    @Transactional(readOnly = true)
    public List<String> getAllSlugs() {
        return productRepository.findAllActiveSlugs();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getFeaturedProducts(int limit) {
        List<Product> featured = productRepository.findFeatured(PageRequest.of(0, limit));

        // NEW LINE — batch-fetch pricingTiers via default_batch_fetch_size=25
        // (aapke application-prod.properties mein already set hai)
        featured.forEach(p -> p.getPricingTiers().size());

        List<Long> ids = featured.stream().map(Product::getId).toList();
        Map<Long, ProductInventory> inventoryMap = inventoryRepository
                .findAllByProductIdIn(ids)
                .stream()
                .collect(Collectors.toMap(i -> i.getProduct().getId(), i -> i));

        return featured.stream()
                .map(p -> toPublicResponse(p, inventoryMap.get(p.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return productRepository.findDistinctCategories();
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN METHODS
    // ═══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public PagedResponse<ProductAdminResponse> listProductsAdmin(int page, int size) {
        Page<Product> products = productRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        // ── Two batch queries replace N*2 queries ─────────────────────────────
        List<Long> ids = products.map(Product::getId).getContent();

        Map<Long, ProductInventory> inventoryMap = inventoryRepository
                .findAllByProductIdIn(ids)
                .stream()
                .collect(Collectors.toMap(i -> i.getProduct().getId(), i -> i));

        // Images grouped by productId, preserving sort_order (query orders by it)
        Map<Long, List<ProductImage>> imagesMap = productImageRepository
                .findAllByProductIdIn(ids)
                .stream()
                .collect(Collectors.groupingBy(img -> img.getProduct().getId()));

        return PagedResponse.from(
                products.map(p -> toAdminResponse(
                        p,
                        inventoryMap.get(p.getId()),
                        imagesMap.getOrDefault(p.getId(), List.of())
                ))
        );
    }

    @Transactional(readOnly = true)
    public ProductAdminResponse getProductAdmin(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        // Single product — individual queries are fine
        ProductInventory inv = inventoryRepository.findByProductId(id).orElse(null);
        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrderAsc(id);
        return toAdminResponse(product, inv, images);
    }

    // ── Create ────────────────────────────────────────────────
    @Transactional
    public ProductAdminResponse createProduct(CreateProductRequest req) {

        String slug = req.getSlug() != null && !req.getSlug().isBlank()
                ? SlugUtils.toSlug(req.getSlug())
                : SlugUtils.toSlug(req.getName());

        if (productRepository.existsBySlug(slug))
            throw new DuplicateResourceException("Product with slug '" + slug + "' already exists");

        String primaryImageUrl = req.getImage();
        List<String> extraUrls = new ArrayList<>();

        if (req.getProductImages() != null && !req.getProductImages().isEmpty()) {
            primaryImageUrl = req.getProductImages().get(0).getUrl();
            req.getProductImages().stream()
                    .skip(1)
                    .map(CreateProductRequest.ProductImageInput::getUrl)
                    .forEach(extraUrls::add);
        } else if (req.getImages() != null) {
            extraUrls.addAll(req.getImages());
        }

        // ── Categories: use the array if the frontend sent one, else fall
        // back to the legacy singular category (older callers, seed scripts) ──
        List<String> cats = req.getCategories(); // @NotEmpty on the DTO guarantees non-empty
        List<String> catSlugs = (req.getCategorySlugs() != null && !req.getCategorySlugs().isEmpty())
                ? req.getCategorySlugs() : cats.stream().map(SlugUtils::toSlug).toList();

        Product product = Product.builder()
                .name(req.getName())
                .slug(slug)
                .category(cats.get(0))
                .categorySlug(catSlugs.get(0))
                .categories(cats)
                .categorySlugs(catSlugs)
                .description(req.getDescription())
                .fullDescription(req.getFullDescription())
                .image(primaryImageUrl)
                .images(extraUrls)
                .moq(req.getMoq())
                .basePrice(req.getBasePrice())
                .material(req.getMaterial())
                .leadTime(req.getLeadTime())
                .brandingOptions(req.getBrandingOptions() != null ? req.getBrandingOptions() : new ArrayList<>())
                .isFeatured(req.getIsFeatured() != null ? req.getIsFeatured() : false)
                .isActive(true)
                .tags(req.getTags() != null ? req.getTags() : new ArrayList<>())
                .metaTitle(req.getMetaTitle())
                .metaDescription(req.getMetaDescription())
                .build();

        if (req.getPricingTiers() != null) {
            List<ProductPricingTier> tiers = req.getPricingTiers().stream()
                    .map(t -> ProductPricingTier.builder()
                            .product(product)
                            .minQty(t.getMinQty())
                            .maxQty(t.getMaxQty())
                            .price(t.getPrice())
                            .label(t.getLabel())
                            .build())
                    .toList();
            product.setPricingTiers(tiers);
        }

        Product saved = productRepository.save(product);

        List<ProductImage> savedImages = List.of();
        if (req.getProductImages() != null && !req.getProductImages().isEmpty()) {
            savedImages = productImageService.buildAndSaveImagesFromPayload(req.getProductImages(), saved);
        }

        CreateProductRequest.InventoryInput inv = req.getInventory();
        inventoryService.createInventoryForProduct(
                saved,
                inv != null && inv.getStockQty()     != null ? inv.getStockQty()     : 0,
                inv != null && inv.getReorderLevel() != null ? inv.getReorderLevel() : 50,
                inv != null && inv.getMaxStockQty()  != null ? inv.getMaxStockQty()  : 10000,
                inv != null ? inv.getSku()            : null,
                inv != null ? inv.getWarehouseNotes() : null
        );

        // Fetch the just-created inventory record to include it in the response
        ProductInventory createdInv = inventoryRepository.findByProductId(saved.getId()).orElse(null);

        log.info("Product created: {} ({})", saved.getName(), saved.getSlug());
        return toAdminResponse(saved, createdInv, savedImages);
    }

    // ── Update ────────────────────────────────────────────────
    @Transactional
    public ProductAdminResponse updateProduct(Long id, UpdateProductRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (req.getName()            != null) product.setName(req.getName());

        if (req.getCategories() != null && !req.getCategories().isEmpty()) {
            List<String> catSlugs = (req.getCategorySlugs() != null && !req.getCategorySlugs().isEmpty())
                    ? req.getCategorySlugs()
                    : req.getCategories().stream().map(SlugUtils::toSlug).toList();
            product.setCategories(req.getCategories());
            product.setCategorySlugs(catSlugs);
            product.setCategory(req.getCategories().get(0));
            product.setCategorySlug(catSlugs.get(0));
        } else {
            if (req.getCategory()     != null) product.setCategory(req.getCategory());
            if (req.getCategorySlug() != null) product.setCategorySlug(req.getCategorySlug());
        }

        if (req.getDescription()     != null) product.setDescription(req.getDescription());
        if (req.getFullDescription() != null) product.setFullDescription(req.getFullDescription());
        if (req.getImage()           != null) product.setImage(req.getImage());
        if (req.getImages()          != null) product.setImages(req.getImages());
        if (req.getMoq()             != null) product.setMoq(req.getMoq());
        if (req.getBasePrice()       != null) product.setBasePrice(req.getBasePrice());
        if (req.getMaterial()        != null) product.setMaterial(req.getMaterial());
        if (req.getLeadTime()        != null) product.setLeadTime(req.getLeadTime());
        if (req.getBrandingOptions() != null) product.setBrandingOptions(req.getBrandingOptions());
        if (req.getIsFeatured()      != null) product.setIsFeatured(req.getIsFeatured());
        if (req.getIsActive()        != null) product.setIsActive(req.getIsActive());
        if (req.getSortOrder()       != null) product.setSortOrder(req.getSortOrder());
        if (req.getTags()            != null) product.setTags(req.getTags());
        if (req.getMetaTitle()       != null) product.setMetaTitle(req.getMetaTitle());
        if (req.getMetaDescription() != null) product.setMetaDescription(req.getMetaDescription());

        Product saved = productRepository.save(product);
        log.info("Product updated: {}", saved.getSlug());

        ProductInventory inv = inventoryRepository.findByProductId(saved.getId()).orElse(null);
        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrderAsc(saved.getId());
        return toAdminResponse(saved, inv, images);
    }

    // ── Update pricing ────────────────────────────────────────
    @Transactional
    public ProductAdminResponse updatePricing(Long id, UpdatePricingRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setBasePrice(req.getBasePrice());
        product.getPricingTiers().clear();
        List<ProductPricingTier> newTiers = req.getPricingTiers().stream()
                .map(t -> ProductPricingTier.builder()
                        .product(product)
                        .minQty(t.getMinQty())
                        .maxQty(t.getMaxQty())
                        .price(t.getPrice())
                        .label(t.getLabel())
                        .sortOrder(t.getSortOrder())
                        .build())
                .toList();
        product.getPricingTiers().addAll(newTiers);

        Product saved = productRepository.save(product);
        log.info("Pricing updated for product: {}", saved.getSlug());

        ProductInventory inv = inventoryRepository.findByProductId(saved.getId()).orElse(null);
        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrderAsc(saved.getId());
        return toAdminResponse(saved, inv, images);
    }

    // ── Soft delete ───────────────────────────────────────────
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setIsActive(false);
        productRepository.save(product);
        log.info("Product soft-deleted: {}", product.getSlug());
    }

    // ── Hard delete ───────────────────────────────────────────
    @Transactional
    public void hardDeleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrderAsc(id);
        for (ProductImage img : images) {
            try {
                if (img.getPublicId() != null && !img.getPublicId().isBlank()) {
                    uploadService.deleteImage(img.getPublicId());
                }
            } catch (Exception e) {
                log.warn("Could not delete Cloudinary image {} for product id={}: {}",
                        img.getPublicId(), id, e.getMessage());
            }
        }

        productRepository.delete(product);
        log.info("Product permanently deleted: id={}", id);
    }

    // ═══════════════════════════════════════════════════════════
    // PRIVATE MAPPERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Public listing/detail mapper.
     * Accepts a pre-loaded inventory record — null means no inventory row exists yet.
     * No DB calls made inside this method.
     */
    private ProductResponse toPublicResponse(Product p, ProductInventory inv) {
        String stockStatus = "IN_STOCK";
        if (inv != null) {
            stockStatus = inv.getAvailableQty() <= 0 ? "OUT_OF_STOCK"
                    : inv.isLowStock() ? "LOW_STOCK"
                    : "IN_STOCK";
        }

        List<ProductResponse.PricingTierDto> tiers = p.getPricingTiers().stream()
                .map(t -> ProductResponse.PricingTierDto.builder()
                        .minQty(t.getMinQty())
                        .maxQty(t.getMaxQty())
                        .price(t.getPrice())
                        .label(t.getLabel())
                        .build())
                .toList();

        return ProductResponse.builder()
                .id(p.getId()).name(p.getName()).slug(p.getSlug())
                .category(p.getCategory()).categorySlug(p.getCategorySlug())
                .categories(p.getCategories()).categorySlugs(p.getCategorySlugs())
                .description(p.getDescription()).fullDescription(p.getFullDescription())
                .image(p.getImage()).images(p.getImages())
                .moq(p.getMoq()).basePrice(p.getBasePrice())
                .material(p.getMaterial()).leadTime(p.getLeadTime())
                .brandingOptions(p.getBrandingOptions())
                .isFeatured(p.getIsFeatured()).tags(p.getTags())
                .pricingTiers(tiers).stockStatus(stockStatus)
                .build();
    }

    /**
     * Admin detail mapper.
     * Accepts pre-loaded inventory and images — no DB calls made inside this method.
     * The public toAdminResponse(Product) overload still works for single-product calls.
     */
    public ProductAdminResponse toAdminResponse(Product p, ProductInventory inv, List<ProductImage> images) {
        List<ProductAdminResponse.PricingTierDto> tiers = p.getPricingTiers().stream()
                .map(t -> ProductAdminResponse.PricingTierDto.builder()
                        .id(t.getId()).minQty(t.getMinQty()).maxQty(t.getMaxQty())
                        .price(t.getPrice()).label(t.getLabel()).sortOrder(t.getSortOrder())
                        .build())
                .toList();

        List<ProductImageResponse> productImages = images.stream()
                .map(productImageService::toResponse)
                .toList();

        ProductAdminResponse.InventoryInfo invInfo = null;
        if (inv != null) {
            int avail = inv.getAvailableQty();
            invInfo = ProductAdminResponse.InventoryInfo.builder()
                    .inventoryId(inv.getId())
                    .stockQty(inv.getStockQty())
                    .reservedQty(inv.getReservedQty())
                    .availableQty(avail)
                    .reorderLevel(inv.getReorderLevel())
                    .maxStockQty(inv.getMaxStockQty())
                    .isLowStock(inv.isLowStock())
                    .stockStatus(avail <= 0 ? "OUT_OF_STOCK" : inv.isLowStock() ? "LOW_STOCK" : "IN_STOCK")
                    .sku(inv.getSku())
                    .lastRestockedAt(inv.getLastRestockedAt())
                    .build();
        }

        return ProductAdminResponse.builder()
                .id(p.getId()).name(p.getName()).slug(p.getSlug())
                .category(p.getCategory()).categorySlug(p.getCategorySlug())
                .categories(p.getCategories()).categorySlugs(p.getCategorySlugs())
                .description(p.getDescription()).fullDescription(p.getFullDescription())
                .image(p.getImage()).images(p.getImages())
                .moq(p.getMoq()).basePrice(p.getBasePrice())
                .material(p.getMaterial()).leadTime(p.getLeadTime())
                .brandingOptions(p.getBrandingOptions())
                .isFeatured(p.getIsFeatured()).isActive(p.getIsActive())
                .sortOrder(p.getSortOrder()).tags(p.getTags())
                .metaTitle(p.getMetaTitle()).metaDescription(p.getMetaDescription())
                .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt())
                .pricingTiers(tiers)
                .productImages(productImages)
                .inventory(invInfo)
                .build();
    }

    /**
     * Convenience overload for single-product endpoints (getProductAdmin, updateProduct, etc.)
     * where batch loading isn't needed. Fetches inventory and images itself.
     */
    public ProductAdminResponse toAdminResponse(Product p) {
        ProductInventory inv = inventoryRepository.findByProductId(p.getId()).orElse(null);
        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrderAsc(p.getId());
        return toAdminResponse(p, inv, images);
    }
}