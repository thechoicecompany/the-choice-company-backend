package com.thechoicecompany.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sample_product_images")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SampleProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sample_product_id", nullable = false)
    private SampleProduct sampleProduct;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "public_id", length = 200)
    private String publicId;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;
}