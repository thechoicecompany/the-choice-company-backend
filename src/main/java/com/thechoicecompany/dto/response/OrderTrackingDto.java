package com.thechoicecompany.dto.response;

import com.thechoicecompany.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class OrderTrackingDto {
    // Safe fields only — no payment IDs, no internal notes exposed
    private String orderId;
    private String customerName;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Shipping
    private String city;
    private String state;
    private String pincode;
    private String trackingNumber;  // from shippingAddress JSONB
    private String courierName;     // from shippingAddress JSONB

    // Items (name + quantity only — no prices)
    private List<Map<String, Object>> items;

    // Financials
    private BigDecimal total;
    private int itemCount;
}