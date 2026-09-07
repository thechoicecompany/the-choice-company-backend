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
public class DemoOrderDetailDto {
    private Long id;
    private String orderId;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private Map<String, Object> shippingAddress;
    private List<Map<String, Object>> items;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private String couponCode;
    private BigDecimal gstAmount;
    private BigDecimal total;
    private OrderStatus status;
    private Boolean emailSent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}