package com.thechoicecompany.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SampleProductAdminResponse {
    private Long id;
    private String name;
    private String slug;
    private String category;
    private String categorySlug;
    private String description;
    private String image;
    private List<String> images;
    private BigDecimal samplePrice;
    private BigDecimal bulkPrice;
    private Integer maxSampleQty;
    private Integer moq;
    private Integer shippingDays;
    private String material;
    private String dimensions;
    private String weight;
    private List<String> brandingOptions;
    private List<String> tags;
    private Boolean isActive;
    private Boolean isBestseller;
    private Integer sortOrder;
    private String metaTitle;
    private String metaDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SampleProductImageResponse> productImages;
}