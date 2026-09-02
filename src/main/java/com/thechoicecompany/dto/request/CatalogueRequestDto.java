package com.thechoicecompany.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CatalogueRequestDto {

    @NotBlank @Email
    private String email;

    @Size(max = 100)
    private String companyName;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String phone;

    // Where the user clicked download: exit_popup | contact_page | blog | footer
    private String source = "exit_popup";

    // Page URL where the user was — for analytics
    private String pageUrl;
}
