package com.thechoicecompany.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Public-facing shape — intentionally matches the old static SampleProduct
 * TS type field-for-field so ShopGrid / ShopProductDetail need zero prop changes.
 */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SampleProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String category;
    private String description;
    private String image;
    private List<String> images;          // full gallery, primary first
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
}