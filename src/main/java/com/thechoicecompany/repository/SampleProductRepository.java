package com.thechoicecompany.repository;

import com.thechoicecompany.entity.SampleProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SampleProductRepository extends JpaRepository<SampleProduct, Long> {

    Optional<SampleProduct> findBySlugAndIsActiveTrue(String slug);

    boolean existsBySlug(String slug);

    @Query("""
        SELECT p FROM SampleProduct p
        WHERE p.isActive = true
          AND (:category IS NULL OR :category = 'All' OR p.category = :category)
        """)
    Page<SampleProduct> findActiveWithFilters(@Param("category") String category, Pageable pageable);

    @Query("SELECT p.slug FROM SampleProduct p WHERE p.isActive = true")
    List<String> findAllActiveSlugs();

    @Query("SELECT DISTINCT p.category FROM SampleProduct p WHERE p.isActive = true ORDER BY p.category")
    List<String> findDistinctActiveCategories();
}