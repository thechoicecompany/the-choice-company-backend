package com.thechoicecompany.dto.response;

import com.thechoicecompany.enums.ContactStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ContactResponse {
    private Long id;
    private String name;
    private String company;
    private String email;
    private String phone;
    private String subject;
    private String message;
    private ContactStatus status;
    private String assignedToName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}