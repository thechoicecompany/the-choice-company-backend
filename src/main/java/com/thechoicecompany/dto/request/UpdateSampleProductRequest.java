package com.thechoicecompany.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * PATCH semantics — every field optional, only non-null fields are applied.
 * Mirrors UpdateProductRequest's contract.
 */
@Data
public class UpdateSampleProductRequest {

    @Size(min = 3, max = 200)
    private String name;

    private String category;
    private String categorySlug;

    @Size(min = 10, max = 500)
    private String description;

    private String image;
    private List<String> images;

    @DecimalMin("1.0")
    private BigDecimal samplePrice;

    @DecimalMin("1.0")
    private BigDecimal bulkPrice;

    @Min(1) @Max(20)
    private Integer maxSampleQty;

    @Min(1) @Max(100000)
    private Integer moq;

    @Min(1) @Max(60)
    private Integer shippingDays;

    private String material;
    private String dimensions;
    private String weight;

    private List<String> brandingOptions;
    private List<String> tags;

    private Boolean isBestseller;
    private Boolean isActive;
    private Integer sortOrder;

    private String metaTitle;
    private String metaDescription;

    /** New images to append — same shape as create. Existing images untouched unless deleted explicitly. */
    private List<CreateSampleProductRequest.ProductImageInput> productImages;
}