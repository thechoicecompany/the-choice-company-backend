package com.thechoicecompany.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * All fields optional — PATCH semantics.
 * Only non-null fields are updated.
 */
@Data
public class UpdateProductRequest {

    @Size(min = 3, max = 200)
    private String name;

    @Size(min = 2, max = 100)
    private String category;

    @Size(min = 2, max = 100)
    private String categorySlug;

    @Size(min = 10, max = 500)
    private String description;

    private String fullDescription;
    private String image;
    private List<String> images;

    @Min(1) @Max(100000)
    private Integer moq;

    @DecimalMin("1.0")
    private BigDecimal basePrice;

    private String material;
    private String leadTime;
    private List<String> brandingOptions;
    private Boolean isFeatured;
    private Boolean isActive;
    private Integer sortOrder;
    private List<String> tags;
    private String metaTitle;
    private String metaDescription;
}
