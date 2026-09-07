package com.thechoicecompany.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GalleryItemDetailDto {
    Long   id;
    String projectName;
    String caption;
    String fileType;
    String thumbnailUrl;
    String fullUrl;    // presigned S3 / CDN URL — only returned here
}