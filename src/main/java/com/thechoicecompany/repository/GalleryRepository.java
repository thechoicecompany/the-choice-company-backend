package com.thechoicecompany.repository;

import com.thechoicecompany.entity.GalleryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GalleryRepository extends JpaRepository<GalleryItem, Long> {
    List<GalleryItem> findByIsActiveTrueOrderBySortOrderAsc();
    List<GalleryItem> findByCategoryAndIsActiveTrueOrderBySortOrderAsc(String category);
}
