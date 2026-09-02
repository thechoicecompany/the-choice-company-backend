package com.thechoicecompany.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdatePricingRequest {

    @NotNull @DecimalMin("1.0")
    private BigDecimal basePrice;

    // Replace all pricing tiers with this new list
    @NotEmpty
    private List<PricingTierInput> pricingTiers;

    @Data
    public static class PricingTierInput {
        @NotNull @Min(1)   private Integer minQty;
        @NotNull @Min(1)   private Integer maxQty;
        @NotNull @DecimalMin("0.01") private BigDecimal price;
        @Size(max = 100)   private String label;
        @Min(0)            private Integer sortOrder = 0;
    }
}
