package com.thechoicecompany.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Mirrors ContactSchema (zod) in lib/api-client.ts exactly, field for field,
// so frontend and backend validation never drift apart.
@Data
public class ContactRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, message = "Name is required")
    private String name;

    private String company; // optional

    @NotBlank(message = "Valid email required")
    @Email(message = "Valid email required")
    private String email;

    private String phone; // optional

    @NotBlank(message = "Please select a subject")
    private String subject;

    @NotBlank(message = "Message must be at least 10 characters")
    @Size(min = 10, max = 2000, message = "Message must be at least 10 characters")
    private String message;
}