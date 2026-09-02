package com.thechoicecompany.dto.response;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SampleProductImageResponse {
    private Long id;
    private String imageUrl;
    private String publicId;
    private Integer sortOrder;
    private Boolean isPrimary;
}