package com.thechoicecompany.repository;

import com.thechoicecompany.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderBySortOrderAsc(Long productId);

    /**
     * Batch-fetches images for a list of product IDs in a single query.
     * Used by ProductService listing methods to avoid N+1.
     * Results are ordered by product_id then sort_order so groupingBy
     * in the service preserves display order within each product.
     */
    @Query("""
        SELECT pi FROM ProductImage pi
        WHERE pi.product.id IN :productIds
        ORDER BY pi.product.id ASC, pi.sortOrder ASC
        """)
    List<ProductImage> findAllByProductIdIn(@Param("productIds") List<Long> productIds);

    @Modifying
    @Query("DELETE FROM ProductImage pi WHERE pi.product.id = :productId")
    void deleteAllByProductId(@Param("productId") Long productId);
}