// src/main/java/com/thechoicecompany/service/CartPricingService.java
package com.thechoicecompany.service;

import com.thechoicecompany.dto.request.ComputePriceRequest;
import com.thechoicecompany.dto.request.ComputePriceRequest.CartItemRequest;
import com.thechoicecompany.dto.response.ComputePriceResponse;
import com.thechoicecompany.entity.SampleProduct;
import com.thechoicecompany.exception.BadRequestException;
import com.thechoicecompany.repository.SampleProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartPricingService {

    private final SampleProductRepository sampleProductRepository;

    private static final Map<String, Integer> COUPON_PERCENT = Map.of(
            "SAMPLE10", 10,
            "FIRST15",  15,
            "TCC20",    20
    );

    private static final long MIN_AMOUNT_PAISE = 100L;

    public ComputePriceResponse computePrice(ComputePriceRequest request) {

        List<CartItemRequest> items = request.getCartItems();

        // ── 1. Fetch all products in one query ──────────────────────────────
        List<Long> productIds = items.stream()
                .map(CartItemRequest::getProductId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, SampleProduct> productMap = sampleProductRepository
                .findAllByIdInAndIsActiveTrue(productIds)
                .stream()
                .collect(Collectors.toMap(SampleProduct::getId, p -> p));

        // ── 2. Validate each line and accumulate subtotal ───────────────────
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItemRequest item : items) {

            if (item.getProductId() == null || item.getQuantity() == null) {
                throw new BadRequestException(
                        "Each cart item must have a productId and quantity");
            }

            SampleProduct product = productMap.get(item.getProductId());
            if (product == null) {
                throw new BadRequestException(
                        "Product not available: id=" + item.getProductId());
            }

            int qty = item.getQuantity();

            if (qty < 1) {
                throw new BadRequestException(
                        "Quantity must be at least 1 for: " + product.getName());
            }

            if (qty > product.getMaxSampleQty()) {
                throw new BadRequestException(
                        "Quantity " + qty + " exceeds sample limit of "
                        + product.getMaxSampleQty() + " for: " + product.getName());
            }

            // ✅ BigDecimal × int — no cast needed, no precision loss
            subtotal = subtotal.add(
                    product.getSamplePrice().multiply(BigDecimal.valueOf(qty))
            );
        }

        // ── 3. Apply coupon ─────────────────────────────────────────────────
        String couponCode    = request.getCouponCode();
        String appliedCoupon = null;
        BigDecimal discount  = BigDecimal.ZERO;

        if (couponCode != null && !couponCode.isBlank()) {
            Integer pct = COUPON_PERCENT.get(couponCode.trim().toUpperCase());
            if (pct != null) {
                discount = subtotal
                        .multiply(BigDecimal.valueOf(pct))
                        .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
                appliedCoupon = couponCode.trim().toUpperCase();
            } else {
                log.debug("Unknown coupon code ignored: '{}'", couponCode);
            }
        }

        // ── 4. Final amounts ────────────────────────────────────────────────
        BigDecimal total = subtotal.subtract(discount);

        // Convert to whole rupees (round down — always favour the merchant)
        long totalRupees      = total.setScale(0, RoundingMode.DOWN).longValue();
        long finalAmountPaise = totalRupees * 100L;

        if (finalAmountPaise < MIN_AMOUNT_PAISE) {
            throw new BadRequestException(
                    "Order total is below the minimum allowed amount");
        }

        log.info("Price computed — subtotal={} discount={} total={} coupon={}",
                subtotal, discount, total, appliedCoupon);

        return ComputePriceResponse.builder()
                .subtotal(subtotal.setScale(0, RoundingMode.DOWN).longValue())
                .discount(discount.setScale(0, RoundingMode.DOWN).longValue())
                .total(totalRupees)
                .finalAmountPaise(finalAmountPaise)
                .appliedCoupon(appliedCoupon)
                .build();
    }
}