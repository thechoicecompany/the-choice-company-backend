package com.thechoicecompany.dto.request;

import com.thechoicecompany.enums.InquiryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateInquiryStatusRequest {
    @NotNull
    private InquiryStatus status;
    private String note;
    private Long assignedTo;
}
