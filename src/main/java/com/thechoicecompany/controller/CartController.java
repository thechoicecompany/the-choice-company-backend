// src/main/java/com/thechoicecompany/controller/CartController.java
package com.thechoicecompany.controller;

import com.thechoicecompany.dto.request.ComputePriceRequest;
import com.thechoicecompany.dto.response.ApiResponse;
import com.thechoicecompany.dto.response.ComputePriceResponse;
import com.thechoicecompany.service.CartPricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartPricingService cartPricingService;

    /**
     * Recomputes the canonical price for a set of cart items server-side.
     * Called by the Next.js API routes before creating a Razorpay order
     * and again before saving the verified order — the client-supplied
     * amount is never trusted for either step.
     *
     * Public endpoint (no JWT required) — the checkout flow is guest-friendly.
     * Rate limiting is handled by the existing RateLimitFilter.
     */
    @PostMapping("/compute-price")
    public ResponseEntity<ApiResponse<ComputePriceResponse>> computePrice(
            @Valid @RequestBody ComputePriceRequest request) {

        ComputePriceResponse price = cartPricingService.computePrice(request);
        return ResponseEntity.ok(ApiResponse.success(price));
    }
}