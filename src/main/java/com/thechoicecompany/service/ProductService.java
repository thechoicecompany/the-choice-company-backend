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
import com.thechoicecompany.exception.BusinessException;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository      productRepository;
    private final InventoryRepository    inventoryRepository;
    private final InventoryService       inventoryService;
    private final ProductImageService    productImageService;    // ← NEW injection
    private final ProductImageRepository productImageRepository; // ← NEW injection

    // ═══════════════════════════════════════════════════════════
    // PUBLIC API METHODS (unchanged — omitted for brevity)
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
        default           -> Sort.by("sort_order").ascending().and(Sort.by("is_featured").descending());
    };
        java.math.BigDecimal minPrice = null, maxPrice = null;
        if (budget != null && !budget.isBlank()) {
            if (budget.endsWith("+")) {
                minPrice = new java.math.BigDecimal(budget.replace("+", ""));
            } else {
                String[] parts = budget.split("-");
                if (parts.length == 2) {
                    minPrice = new java.math.BigDecimal(parts[0]);
                    maxPrice = new java.math.BigDecimal(parts[1]);
                }
            }
        }

        Page<Product> products = productRepository.findWithFilters(
            category, featured, occasion, minPrice, maxPrice, moq,
            PageRequest.of(page, size, sorting));

        // ── Force-initialize pricingTiers for each product ────────────────────
        // Required because the native query can't use @EntityGraph.
        // Hibernate will batch these if spring.jpa.properties.hibernate.default_batch_fetch_size is set.
        products.forEach(p -> p.getPricingTiers().size());

        return PagedResponse.from(products.map(p -> toPublicResponse(p, true)));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlugAndIsActiveTrue(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
        return toPublicResponse(product, true);
    }

    @Transactional(readOnly = true)
    public List<String> getAllSlugs() { return productRepository.findAllActiveSlugs(); }

    @Transactional(readOnly = true)
    public List<ProductResponse> getFeaturedProducts(int limit) {
        return productRepository.findFeatured(PageRequest.of(0, limit))
            .stream().map(p -> toPublicResponse(p, false)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getCategories() { return productRepository.findDistinctCategories(); }

    // ═══════════════════════════════════════════════════════════
    // ADMIN METHODS
    // ═══════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public PagedResponse<ProductAdminResponse> listProductsAdmin(int page, int size) {
        Page<Product> products = productRepository.findAll(
            PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PagedResponse.from(products.map(this::toAdminResponse));
    }

    @Transactional(readOnly = true)
    public ProductAdminResponse getProductAdmin(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return toAdminResponse(product);
    }

    // ── Create new product ─────────────────────────────────────
    @Transactional
    public ProductAdminResponse createProduct(CreateProductRequest req) {

        String slug = req.getSlug() != null && !req.getSlug().isBlank()
            ? SlugUtils.toSlug(req.getSlug())
            : SlugUtils.toSlug(req.getName());

        if (productRepository.existsBySlug(slug))
            throw new DuplicateResourceException("Product with slug '" + slug + "' already exists");

        // ── Derive primary image URL ──────────────────────────────────────────
        // If productImages supplied, use the first one's URL as the legacy image field.
        // This keeps backward compat with the products.image column.
        String primaryImageUrl = req.getImage();
        if (req.getProductImages() != null && !req.getProductImages().isEmpty()) {
            req.getProductImages().stream()
                .filter(pi -> pi.getSortOrder() == 0)
                .findFirst()
                .ifPresent(pi -> { /* will be set below */ });
            primaryImageUrl = req.getProductImages().get(0).getUrl();
        }

        // ── Extra image URLs for the legacy List<String> column ───────────────
        List<String> extraUrls = new ArrayList<>();
        if (req.getProductImages() != null && req.getProductImages().size() > 1) {
            req.getProductImages().stream()
                .skip(1)
                .map(pi -> pi.getUrl())
                .forEach(extraUrls::add);
        } else if (req.getImages() != null) {
            extraUrls.addAll(req.getImages());
        }

        Product product = Product.builder()
            .name(req.getName())
            .slug(slug)
            .category(req.getCategory())
            .categorySlug(req.getCategorySlug())
            .description(req.getDescription())
            .fullDescription(req.getFullDescription())
            .image(primaryImageUrl)                         // ← derived above
            .images(extraUrls)                              // ← derived above
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
                    .product(product).minQty(t.getMinQty())
                    .maxQty(t.getMaxQty()).price(t.getPrice())
                    .label(t.getLabel()).build())
                .collect(Collectors.toList());
            product.setPricingTiers(tiers);
        }

        Product saved = productRepository.save(product);

        // ── Save ProductImage records ──────────────────────────────────────────
        // Only when the frontend has pre-uploaded images (the primary flow).
        if (req.getProductImages() != null && !req.getProductImages().isEmpty()) {
            productImageService.buildAndSaveImagesFromPayload(req.getProductImages(), saved);
        }

        // ── Auto-create inventory ─────────────────────────────────────────────
     // ── Auto-create inventory ─────────────────────────────────────────────
        CreateProductRequest.InventoryInput inv = req.getInventory();

        Integer stockQty       = (inv != null && inv.getStockQty()     != null) ? inv.getStockQty()     : 0;
        Integer reorderLevel   = (inv != null && inv.getReorderLevel() != null) ? inv.getReorderLevel() : 50;
        Integer maxStockQty    = (inv != null && inv.getMaxStockQty()  != null) ? inv.getMaxStockQty()  : 10000;
        String  sku            = (inv != null) ? inv.getSku()            : null;
        String  warehouseNotes = (inv != null) ? inv.getWarehouseNotes() : null;

        inventoryService.createInventoryForProduct(
            saved, stockQty, reorderLevel, maxStockQty, sku, warehouseNotes
        );

        log.info("Product created: {} ({})", saved.getName(), saved.getSlug());
        return toAdminResponse(saved);
    }

    // ── Update existing product (unchanged) ───────────────────
    @Transactional
    public ProductAdminResponse updateProduct(Long id, UpdateProductRequest req) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (req.getName()            != null) product.setName(req.getName());
        if (req.getCategory()        != null) product.setCategory(req.getCategory());
        if (req.getCategorySlug()    != null) product.setCategorySlug(req.getCategorySlug());
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
        return toAdminResponse(saved);
    }

    // ── Update pricing (unchanged) ────────────────────────────
    @Transactional
    public ProductAdminResponse updatePricing(Long id, UpdatePricingRequest req) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setBasePrice(req.getBasePrice());
        product.getPricingTiers().clear();
        List<ProductPricingTier> newTiers = req.getPricingTiers().stream()
            .map(t -> ProductPricingTier.builder()
                .product(product).minQty(t.getMinQty())
                .maxQty(t.getMaxQty()).price(t.getPrice())
                .label(t.getLabel()).sortOrder(t.getSortOrder()).build())
            .collect(Collectors.toList());
        product.getPricingTiers().addAll(newTiers);

        Product saved = productRepository.save(product);
        log.info("Pricing updated for product: {}", saved.getSlug());
        return toAdminResponse(saved);
    }

    // ── Soft delete (unchanged) ───────────────────────────────
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        product.setIsActive(false);
        productRepository.save(product);
        log.info("Product soft-deleted: {}", product.getSlug());
    }

    // ── Hard delete (unchanged) ───────────────────────────────
    @Transactional
    public void hardDeleteProduct(Long id) {
        if (!productRepository.existsById(id))
            throw new ResourceNotFoundException("Product", "id", id);
        productRepository.deleteById(id);
        log.info("Product permanently deleted: id={}", id);
    }

    // ═══════════════════════════════════════════════════════════
    // PRIVATE MAPPERS
    // ═══════════════════════════════════════════════════════════

    private ProductResponse toPublicResponse(Product p, boolean includeInventory) {
        String stockStatus = "IN_STOCK";
        if (includeInventory) {
            var invOpt = inventoryRepository.findByProductId(p.getId());
            if (invOpt.isPresent()) {
                ProductInventory inv = invOpt.get();
                stockStatus = inv.getAvailableQty() <= 0 ? "OUT_OF_STOCK"
                    : inv.isLowStock() ? "LOW_STOCK" : "IN_STOCK";
            }
        }
        List<ProductResponse.PricingTierDto> tiers = p.getPricingTiers().stream()
            .map(t -> ProductResponse.PricingTierDto.builder()
                .minQty(t.getMinQty()).maxQty(t.getMaxQty())
                .price(t.getPrice()).label(t.getLabel()).build())
            .collect(Collectors.toList());

        return ProductResponse.builder()
            .id(p.getId()).name(p.getName()).slug(p.getSlug())
            .category(p.getCategory()).categorySlug(p.getCategorySlug())
            .description(p.getDescription()).fullDescription(p.getFullDescription())
            .image(p.getImage()).images(p.getImages())
            .moq(p.getMoq()).basePrice(p.getBasePrice())
            .material(p.getMaterial()).leadTime(p.getLeadTime())
            .brandingOptions(p.getBrandingOptions())
            .isFeatured(p.getIsFeatured()).tags(p.getTags())
            .pricingTiers(tiers).stockStatus(stockStatus)
            .build();
    }

    public ProductAdminResponse toAdminResponse(Product p) {
        List<ProductAdminResponse.PricingTierDto> tiers = p.getPricingTiers().stream()
            .map(t -> ProductAdminResponse.PricingTierDto.builder()
                .id(t.getId()).minQty(t.getMinQty()).maxQty(t.getMaxQty())
                .price(t.getPrice()).label(t.getLabel()).sortOrder(t.getSortOrder()).build())
            .collect(Collectors.toList());

        // ── NEW: load ProductImage records ────────────────────────────────────
        List<ProductImageResponse> productImages = productImageRepository
            .findByProductIdOrderBySortOrderAsc(p.getId())
            .stream()
            .map(productImageService::toResponse)
            .collect(Collectors.toList());

        ProductAdminResponse.InventoryInfo invInfo = null;
        var invOpt = inventoryRepository.findByProductId(p.getId());
        if (invOpt.isPresent()) {
            ProductInventory inv = invOpt.get();
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
            .productImages(productImages)               // ← NEW
            .inventory(invInfo)
            .build();
    }
}