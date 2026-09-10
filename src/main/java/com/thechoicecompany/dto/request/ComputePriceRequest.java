// src/main/java/com/thechoicecompany/dto/request/ComputePriceRequest.java
package com.thechoicecompany.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ComputePriceRequest {

    @NotEmpty(message = "Cart items must not be empty")
    @Valid
    private List<CartItemRequest> cartItems;

    // nullable — no coupon code is fine
    private String couponCode;

    @Data
    public static class CartItemRequest {

        private Long productId;

        private Integer quantity;
    }
}