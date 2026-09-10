// src/main/java/com/thechoicecompany/dto/response/ComputePriceResponse.java
package com.thechoicecompany.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComputePriceResponse {

    /** Sum of (samplePrice × quantity) for all items — before discount. */
    private long subtotal;

    /** Rupee amount saved by the coupon (0 if no valid coupon). */
    private long discount;

    /** subtotal − discount — what the customer actually pays in ₹. */
    private long total;

    /**
     * total × 100 — paise value passed directly to Razorpay.
     * Razorpay requires the smallest currency unit; never multiply again.
     */
    private long finalAmountPaise;

    /** The coupon code that was actually applied (null if none / invalid). */
    private String appliedCoupon;
}