package com.thechoicecompany.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GalleryItemUpdateRequest {
    @NotBlank @Size(max = 200) private String projectName;
    @NotBlank @Size(max = 300) private String caption;
    @NotBlank @Size(max = 50)  private String category;
    @Size(max = 100)           private String clientIndustry;
                               private Integer quantity;
                               private Integer sortOrder;
}