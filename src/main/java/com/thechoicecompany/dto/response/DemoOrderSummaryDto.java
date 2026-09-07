package com.thechoicecompany.dto.response;

import com.thechoicecompany.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class DemoOrderSummaryDto {
    private String orderId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private BigDecimal total;
    private OrderStatus status;
    private Boolean emailSent;
    private LocalDateTime createdAt;
    private int itemCount;
}