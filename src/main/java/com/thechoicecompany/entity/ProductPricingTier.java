package com.thechoicecompany.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_pricing_tiers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductPricingTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "min_qty", nullable = false)
    private Integer minQty;

    @Column(name = "max_qty", nullable = false)
    private Integer maxQty;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 100)
    private String label;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
}
