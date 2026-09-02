package com.thechoicecompany.repository;

import com.thechoicecompany.entity.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<ProductInventory, Long> {

    Optional<ProductInventory> findByProductId(Long productId);

    Optional<ProductInventory> findByProductSlug(String slug);

    boolean existsByProductId(Long productId);

    @Query("SELECT i FROM ProductInventory i WHERE (i.stockQty - i.reservedQty) <= i.reorderLevel")
    List<ProductInventory> findLowStockItems();

    @Query("SELECT i FROM ProductInventory i WHERE (i.stockQty - i.reservedQty) <= 0")
    List<ProductInventory> findOutOfStockItems();

    @Query("SELECT COUNT(i) FROM ProductInventory i WHERE (i.stockQty - i.reservedQty) <= i.reorderLevel")
    Long countLowStockItems();

    @Query("SELECT COUNT(i) FROM ProductInventory i WHERE (i.stockQty - i.reservedQty) <= 0")
    Long countOutOfStockItems();

    @Query("SELECT SUM(i.stockQty) FROM ProductInventory i")
    Long getTotalStockUnits();

    @Query("SELECT i FROM ProductInventory i JOIN FETCH i.product p WHERE p.isActive = :active ORDER BY p.name ASC")
    List<ProductInventory> findAllWithProduct(@Param("active") boolean active);
}