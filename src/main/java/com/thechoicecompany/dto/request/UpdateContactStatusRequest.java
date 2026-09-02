package com.thechoicecompany.dto.request;

import com.thechoicecompany.enums.ContactStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateContactStatusRequest {

    @NotNull
    private ContactStatus status;

    // Optional — admin id to assign this message to (mirrors Inquiry's assignedTo)
    private Long assignedTo;
}