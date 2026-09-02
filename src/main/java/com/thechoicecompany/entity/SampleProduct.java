package com.thechoicecompany.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample-shop product (demo purchase catalogue).
 * Deliberately separate from `Product` (the bulk-order catalogue) — different
 * pricing model (single samplePrice, not tiered) and different lifecycle.
 */
@Entity
@Table(name = "sample_products")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SampleProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "category_slug", length = 100)
    private String categorySlug;

    @Column(nullable = false, length = 500)
    private String description;

    // Primary image URL — kept for fast list rendering without a join.
    @Column(nullable = false, length = 500)
    private String image;

    // Extra gallery URLs (legacy flat list, mirrors Product.images).
    // Full SampleProductImage records (with publicId etc.) live in a separate table.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Column(name = "sample_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal samplePrice;

    @Column(name = "bulk_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal bulkPrice;

    @Column(name = "max_sample_qty", nullable = false)
    @Builder.Default
    private Integer maxSampleQty = 5;

    @Column(nullable = false)
    private Integer moq;

    @Column(name = "shipping_days", nullable = false)
    @Builder.Default
    private Integer shippingDays = 5;

    private String material;
    private String dimensions;
    private String weight;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "branding_options", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> brandingOptions = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_bestseller", nullable = false)
    @Builder.Default
    private Boolean isBestseller = false;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "meta_title", length = 200)
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}