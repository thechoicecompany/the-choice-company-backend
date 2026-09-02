package com.thechoicecompany.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class DemoOrderRequest {

    @NotBlank
    private String orderId;

    private String razorpayOrderId;
    private String razorpayPaymentId;

    @NotBlank
    private String customerName;

    @NotBlank @Email
    private String customerEmail;

    @NotBlank
    private String customerPhone;

    @NotNull
    private Map<String, Object> shippingAddress;

    @NotEmpty
    private List<Map<String, Object>> items;

    @NotNull @DecimalMin("0.0")
    private BigDecimal subtotal;

    private BigDecimal discount = BigDecimal.ZERO;
    private String couponCode;

    @NotNull
    private BigDecimal gstAmount;

    @NotNull
    private BigDecimal total;
}
