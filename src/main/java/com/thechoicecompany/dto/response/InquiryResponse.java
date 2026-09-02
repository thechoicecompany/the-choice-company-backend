package com.thechoicecompany.dto.response;

import com.thechoicecompany.enums.InquirySource;
import com.thechoicecompany.enums.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InquiryResponse {
    private Long id;
    private String refNumber;
    private String companyName;
    private String contactPerson;
    private String designation;
    private String email;
    private String mobile;
    private String city;
    private String state;
    private String productCategory;
    private Integer quantityRequired;
    private String budgetRange;
    private String deliveryLocation;
    private Boolean brandingRequired;
    private String additionalNotes;
    private String logoUrl;
    private InquirySource source;
    private InquiryStatus status;
    private String assignedToName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
