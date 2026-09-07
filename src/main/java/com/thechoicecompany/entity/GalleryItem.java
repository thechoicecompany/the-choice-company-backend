package com.thechoicecompany.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime; 
@Entity
@Table(name = "gallery_items")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GalleryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String caption;

    @Column(name = "project_name", nullable = false, length = 200)
    private String projectName;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "client_industry", length = 100)
    private String clientIndustry;

    private Integer quantity;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Cloudinary — replaces old fileKey/thumbnailKey (local disk) ────────
    @Column(name = "file_type", nullable = false, length = 20)
    private String fileType;          // always "pdf" for now, kept for future image/video items

    @Column(name = "thumbnail_url", nullable = false, length = 500)
    private String thumbnailUrl;      // Cloudinary image URL — shown on public gallery grid

    @Column(name = "thumbnail_public_id", length = 300)
    private String thumbnailPublicId; // for delete/replace

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;           // Cloudinary raw URL — the actual PDF, gated

    @Column(name = "file_public_id", length = 300)
    private String filePublicId;
}

