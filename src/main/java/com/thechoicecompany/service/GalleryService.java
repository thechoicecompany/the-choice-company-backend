package com.thechoicecompany.service;

import com.thechoicecompany.entity.GalleryItem;
import com.thechoicecompany.repository.GalleryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GalleryService {

    private final GalleryRepository galleryRepository;

    @Transactional(readOnly = true)
    public List<GalleryItem> getItems(String category) {
        return category != null && !category.isBlank()
            ? galleryRepository.findByCategoryAndIsActiveTrueOrderBySortOrderAsc(category)
            : galleryRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }
}
