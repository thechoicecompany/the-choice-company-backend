package com.thechoicecompany.service;

import com.thechoicecompany.dto.request.GalleryItemUpdateRequest;
import com.thechoicecompany.dto.response.GalleryItemDto;
import com.thechoicecompany.entity.GalleryItem;
import com.thechoicecompany.exception.ResourceNotFoundException;
import com.thechoicecompany.repository.GalleryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminGalleryService {

    private final GalleryRepository galleryRepository;
    private final UploadService uploadService;

    @Transactional(readOnly = true)
    public List<GalleryItemDto> listAll() {
        return galleryRepository.findAll().stream()
                .map(GalleryItemDto::from)
                .toList();
    }

    @Transactional
    public GalleryItemDto create(MultipartFile thumbnail, MultipartFile pdf,
                                  String projectName, String caption,
                                  String category, String clientIndustry,
                                  String quantityStr, String sortOrderStr) {

        // Thumbnail first — single call, captures both url AND publicId
        Map<String, String> thumbUpload = uploadService.uploadImageWithPublicId(thumbnail, "tcc/gallery");

        // PDF second — roll back the thumbnail if this fails
        Map<String, String> pdfUpload;
        try {
            pdfUpload = uploadService.uploadRawFile(pdf, "tcc/gallery");
        } catch (RuntimeException ex) {
            log.error("PDF upload failed after thumbnail succeeded — rolling back thumbnail {}", thumbUpload.get("publicId"));
            uploadService.deleteImage(thumbUpload.get("publicId"));
            throw ex;
        }

        GalleryItem item = GalleryItem.builder()
                .projectName(projectName)
                .caption(caption)
                .category(category)
                .clientIndustry(clientIndustry)
                .fileType("pdf")
                .thumbnailUrl(thumbUpload.get("url"))
                .thumbnailPublicId(thumbUpload.get("publicId"))
                .fileUrl(pdfUpload.get("url"))
                .filePublicId(pdfUpload.get("publicId"))
                .quantity(parseIntOrNull(quantityStr))
                .sortOrder(parseIntOrDefault(sortOrderStr, 0))
                .isActive(true)
                .build();

        GalleryItem saved = galleryRepository.save(item);
        log.info("Gallery item created: {} (thumb={}, pdf={})",
            saved.getProjectName(), thumbUpload.get("publicId"), pdfUpload.get("publicId"));

        return GalleryItemDto.from(saved);
    }

    @Transactional
    public GalleryItemDto update(Long id, GalleryItemUpdateRequest req) {
        GalleryItem item = findOrThrow(id);
        item.setProjectName(req.getProjectName());
        item.setCaption(req.getCaption());
        item.setCategory(req.getCategory());
        item.setClientIndustry(req.getClientIndustry());
        item.setQuantity(req.getQuantity());
        if (req.getSortOrder() != null) item.setSortOrder(req.getSortOrder());
        return GalleryItemDto.from(item);
    }

    @Transactional
    public GalleryItemDto toggleActive(Long id) {
        GalleryItem item = findOrThrow(id);
        item.setIsActive(!item.getIsActive());
        return GalleryItemDto.from(item);
    }

    @Transactional
    public void delete(Long id) {
        GalleryItem item = findOrThrow(id);
        uploadService.deleteImage(item.getThumbnailPublicId());
        uploadService.deleteRawFile(item.getFilePublicId());
        galleryRepository.delete(item);
        log.info("Gallery item deleted: id={}", id);
    }

    private GalleryItem findOrThrow(Long id) {
        return galleryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GalleryItem", "id", id));
    }

    private Integer parseIntOrNull(String s) {
        try { return s != null && !s.isBlank() ? Integer.parseInt(s.trim()) : null; }
        catch (NumberFormatException e) { return null; }
    }

    private int parseIntOrDefault(String s, int def) {
        try { return s != null && !s.isBlank() ? Integer.parseInt(s.trim()) : def; }
        catch (NumberFormatException e) { return def; }
    }
}