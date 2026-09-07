package com.thechoicecompany.dto.request;

import com.thechoicecompany.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    @NotNull(message = "Status is required")
    private OrderStatus status;

    private String trackingNumber;
    private String courierName;
    private String note;
}