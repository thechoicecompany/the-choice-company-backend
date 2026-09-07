package com.thechoicecompany.service;

import com.thechoicecompany.dto.response.GalleryItemDetailDto;
import com.thechoicecompany.dto.response.GalleryItemDto;
import com.thechoicecompany.entity.GalleryItem;
import com.thechoicecompany.exception.ResourceNotFoundException;
import com.thechoicecompany.repository.GalleryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.thechoicecompany.exception.ResourceNotFoundException;
import java.time.Duration;
import java.util.List;
@Service
@RequiredArgsConstructor
public class GalleryService {
    private final GalleryRepository galleryRepository;

    @Transactional(readOnly = true)
    public List<GalleryItemDto> getItems(String category) {
        List<GalleryItem> items = (category != null && !category.isBlank())
            ? galleryRepository.findByCategoryAndIsActiveTrueOrderBySortOrderAsc(category)
            : galleryRepository.findByIsActiveTrueOrderBySortOrderAsc();
        return items.stream()
            .map(GalleryItemDto::from)   // maps id, caption, thumbnailUrl, etc — no fileUrl here
            .toList();
    }

    @Transactional(readOnly = true)
    public GalleryItemDetailDto getItemDetail(Long id) {
        GalleryItem item = galleryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Gallery item not found: " + id));
        return GalleryItemDetailDto.builder()
            .id(item.getId())
            .projectName(item.getProjectName())
            .caption(item.getCaption())
            .fileType(item.getFileType())
            .thumbnailUrl(item.getThumbnailUrl())
            .fullUrl(item.getFileUrl())   // Cloudinary URL, straight from DB — no presigning needed
            .build();
    }

    @Transactional
    public GalleryItem createItem(GalleryItem item) {
        return galleryRepository.save(item);
    }
}