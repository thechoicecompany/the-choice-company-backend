package com.thechoicecompany.entity;

import com.thechoicecompany.enums.InquirySource;
import com.thechoicecompany.enums.InquiryStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inquiries")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ref_number", unique = true, nullable = false, length = 20)
    private String refNumber;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(name = "contact_person", nullable = false, length = 100)
    private String contactPerson;

    @Column(length = 100)
    private String designation;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 15)
    private String mobile;

    @Column(nullable = false, length = 60)
    private String city;

    @Column(nullable = false, length = 60)
    private String state;

    @Column(name = "product_category", nullable = false, length = 100)
    private String productCategory;

    @Column(name = "quantity_required", nullable = false)
    private Integer quantityRequired;

    @Column(name = "budget_range", nullable = false, length = 30)
    private String budgetRange;

    @Column(name = "delivery_location", nullable = false, length = 200)
    private String deliveryLocation;

    @Column(name = "branding_required")
    @Builder.Default
    private Boolean brandingRequired = false;

    @Column(name = "packaging_requirement", columnDefinition = "TEXT")
    private String packagingRequirement;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @Builder.Default
    private InquirySource source = InquirySource.WEBSITE_FORM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private InquiryStatus status = InquiryStatus.NEW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(name = "whatsapp_sent")
    @Builder.Default
    private Boolean whatsappSent = false;

    @Column(name = "email_sent")
    @Builder.Default
    private Boolean emailSent = false;

    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InquiryNote> notes = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
