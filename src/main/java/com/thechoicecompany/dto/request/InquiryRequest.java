package com.thechoicecompany.dto.request;

import com.thechoicecompany.enums.InquirySource;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InquiryRequest {

    @NotBlank @Size(min=2, max=100)
    private String companyName;

    @NotBlank @Size(min=2, max=100)
    private String contactPerson;

    @Size(max=100)
    private String designation;

    @NotBlank @Email
    private String email;

    @NotBlank @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String mobile;

    @NotBlank @Size(min=2, max=60)
    private String city;

    @NotBlank @Size(min=2, max=60)
    private String state;

    @NotBlank
    private String productCategory;

    @NotNull @Min(50) @Max(500000)
    private Integer quantityRequired;

    @NotBlank
    private String budgetRange;

    @NotBlank @Size(min=2, max=200)
    private String deliveryLocation;

    private Boolean brandingRequired = false;

    @Size(max=500)
    private String packagingRequirement;

    private LocalDate expectedDeliveryDate;

    @Size(max=1000)
    private String additionalNotes;

    @Size(max=500)
    private String logoUrl;

    private String refNumber;

    private InquirySource source = InquirySource.WEBSITE_FORM;
}
