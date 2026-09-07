package com.thechoicecompany.repository;

import com.thechoicecompany.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"pricingTiers"})
    Optional<Product> findBySlugAndIsActiveTrue(String slug);

    boolean existsBySlug(String slug);

    @Query("SELECT p.slug FROM Product p WHERE p.isActive = true")
    List<String> findAllActiveSlugs();

    @EntityGraph(attributePaths = {"pricingTiers"})
    @Query("SELECT p FROM Product p WHERE p.isFeatured = true AND p.isActive = true ORDER BY p.sortOrder ASC")
    List<Product> findFeatured(Pageable pageable);

    // ── No @EntityGraph here — native queries cannot use EntityGraphs ─────
    @Query(value = """
        SELECT * FROM public.products p
        WHERE p.is_active = true
          AND (:category IS NULL OR p.category_slug = :category)
          AND (:featured  IS NULL OR p.is_featured  = :featured)
          AND (:occasion  IS NULL OR p.tags::text   LIKE CONCAT('%"', :occasion, '"%'))
          AND (:minPrice  IS NULL OR p.base_price  >= :minPrice)
          AND (:maxPrice  IS NULL OR p.base_price  <= :maxPrice)
          AND (:moq       IS NULL OR p.moq         <= :moq)
        """,
        countQuery = """
        SELECT COUNT(*) FROM public.products p
        WHERE p.is_active = true
          AND (:category IS NULL OR p.category_slug = :category)
          AND (:featured  IS NULL OR p.is_featured  = :featured)
          AND (:occasion  IS NULL OR p.tags::text   LIKE CONCAT('%"', :occasion, '"%'))
          AND (:minPrice  IS NULL OR p.base_price  >= :minPrice)
          AND (:maxPrice  IS NULL OR p.base_price  <= :maxPrice)
          AND (:moq       IS NULL OR p.moq         <= :moq)
        """,
        nativeQuery = true)
    Page<Product> findWithFilters(
        @Param("category") String category,
        @Param("featured")  Boolean featured,
        @Param("occasion")  String occasion,
        @Param("minPrice")  java.math.BigDecimal minPrice,
        @Param("maxPrice")  java.math.BigDecimal maxPrice,
        @Param("moq")       Integer moq,
        Pageable pageable
    );

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.isActive = true ORDER BY p.category")
    List<String> findDistinctCategories();
}