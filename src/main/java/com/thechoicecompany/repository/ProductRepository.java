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

    @EntityGraph(attributePaths = {"pricingTiers"})
    @Query("SELECT p FROM Product p WHERE " +
           "p.isActive = true AND " +
           "(:category IS NULL OR p.categorySlug = :category) AND " +
           "(:featured IS NULL OR p.isFeatured = :featured)")
    Page<Product> findWithFilters(
        @Param("category") String category,
        @Param("featured") Boolean featured,
        Pageable pageable
    );

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.isActive = true ORDER BY p.category")
    List<String> findDistinctCategories();
}