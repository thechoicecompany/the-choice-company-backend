package com.thechoicecompany.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Tracks every catalogue download / request on the website.
 * Used by admin dashboard to see how many visitors requested the catalogue
 * and what context they came from (exit popup, contact page, blog, etc).
 */
@Entity
@Table(name = "catalogue_requests")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CatalogueRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(name = "phone", length = 15)
    private String phone;

    // Where the request came from: exit_popup | contact_page | blog | footer | direct
    @Column(nullable = false, length = 50)
    @Builder.Default
    private String source = "exit_popup";

    // Page URL where the user was when they requested the catalogue
    @Column(name = "page_url", length = 500)
    private String pageUrl;

    // IP address for deduplication
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    // Whether we actually sent the catalogue email
    @Column(name = "email_sent")
    @Builder.Default
    private Boolean emailSent = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
