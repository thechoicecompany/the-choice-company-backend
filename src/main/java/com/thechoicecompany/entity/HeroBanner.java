package com.thechoicecompany.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hero_banners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeroBanner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Short eyebrow label, e.g. "DIWALI 2025" */
    @Column(nullable = false, length = 120)
    private String tag;

    /** First line of the headline */
    @Column(nullable = false, length = 200)
    private String headlineTop;

    /** Second line of the headline (shown in accent colour) */
    @Column(nullable = false, length = 200)
    private String headlineBottom;

    /** Sub-copy beneath the headline */
    @Column(nullable = false, length = 400)
    private String body;

    /** Cloudinary secure URL */
    @Column(nullable = false)
    private String imageUrl;

    /** Cloudinary public_id — required for deletion/replacement */
    @Column(nullable = false)
    private String imagePublicId;

    /** Lower number = shown first */
    @Column(nullable = false)
    private Integer displayOrder;

    /** Only banners with active=true are served to the public site */
    @Column(nullable = false)
    private Boolean active;

    /** Optional: link for the primary CTA button */
    @Column(length = 300)
    private String ctaLink;

    /** Label for the primary CTA button */
    @Column(length = 80)
    private String ctaLabel;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) active = true;
        if (displayOrder == null) displayOrder = 0;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}