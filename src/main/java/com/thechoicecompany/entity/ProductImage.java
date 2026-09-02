package com.thechoicecompany.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Stores image metadata for a product.
 * Actual image bytes are in Cloudinary — only URL + public_id stored here.
 *
 * product_images table:
 *   id, product_id, image_url, public_id, sort_order, is_primary, created_at
 *
 * Relationship: Product 1 ---- N ProductImage
 */
@Entity
@Table(name = "product_images",
    indexes = {
        @Index(name = "idx_product_images_product_id", columnList = "product_id"),
        @Index(name = "idx_product_images_sort",       columnList = "product_id, sort_order"),
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Parent product — cascade delete removes images when product is deleted */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Cloudinary HTTPS URL — this is what the frontend renders */
    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    /**
     * Cloudinary public_id — needed to delete the image from Cloudinary later.
     * Format: "tcc/products/<uuid>"
     */
    @Column(name = "public_id", nullable = false, length = 500)
    private String publicId;

    /**
     * Display order — 0-indexed.
     * Frontend controls sort order via drag-and-drop.
     * Backend assigns on create; admin can reorder later.
     */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * Whether this is the primary/featured image shown in listings.
     * Exactly ONE image per product should have isPrimary = true.
     * Backend enforces this — frontend cannot set both to true.
     */
    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
