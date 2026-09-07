package com.thechoicecompany.dto.response;

import com.thechoicecompany.entity.GalleryItem;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GalleryItemDto {
    Long id;
    String caption;
    String projectName;
    String category;
    String clientIndustry;
    Integer quantity;
    Integer sortOrder;
    String fileType;
    String thumbnailUrl;
    Boolean isActive;

    public static GalleryItemDto from(GalleryItem g) {
        return GalleryItemDto.builder()
            .id(g.getId())
            .caption(g.getCaption())
            .projectName(g.getProjectName())
            .category(g.getCategory())
            .clientIndustry(g.getClientIndustry())
            .quantity(g.getQuantity())
            .sortOrder(g.getSortOrder())
            .fileType(g.getFileType())
            .thumbnailUrl(g.getThumbnailUrl())
            .isActive(g.getIsActive())
            .build();
    }
}