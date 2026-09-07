package com.thechoicecompany.controller;

import com.thechoicecompany.dto.response.ApiResponse;
import com.thechoicecompany.dto.response.OrderTrackingDto;
import com.thechoicecompany.entity.DemoOrder;
import com.thechoicecompany.exception.ResourceNotFoundException;
import com.thechoicecompany.repository.OrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders/track")
@RequiredArgsConstructor
@Tag(name = "Order Tracking", description = "Public order tracking — no auth required")
public class OrderTrackingController {

    private final OrderRepository orderRepository;

    @GetMapping
    @Operation(summary = "Track order by orderId + email — public endpoint")
    public ResponseEntity<ApiResponse<OrderTrackingDto>> trackOrder(
            @RequestParam String orderId,
            @RequestParam String email) {

        // Both orderId AND email must match — prevents guessing order IDs
        DemoOrder order = orderRepository.findByOrderId(orderId.trim())
            .orElseThrow(() -> new ResourceNotFoundException(
                "No order found with this ID and email combination"));

        if (!order.getCustomerEmail().equalsIgnoreCase(email.trim())) {
            // Return same error — don't reveal whether orderId exists
            throw new ResourceNotFoundException(
                "No order found with this ID and email combination");
        }

        return ResponseEntity.ok(ApiResponse.success(toTrackingDto(order), "Order found"));
    }

    @SuppressWarnings("unchecked")
    private OrderTrackingDto toTrackingDto(DemoOrder o) {
        Map<String, Object> addr = o.getShippingAddress() instanceof Map m ? m : Map.of();
        List<Map<String, Object>> items = o.getItems() instanceof List l ? l : List.of();

        // Strip sensitive fields from items — only expose name + quantity
        List<Map<String, Object>> safeItems = items.stream()
            .map(item -> Map.<String, Object>of(
                "name",     item.getOrDefault("name", ""),
                "quantity", item.getOrDefault("quantity", 1)
            ))
            .toList();

        return OrderTrackingDto.builder()
            .orderId(o.getOrderId())
            .customerName(o.getCustomerName())
            .status(o.getStatus())
            .createdAt(o.getCreatedAt())
            .updatedAt(o.getUpdatedAt())
            .city(String.valueOf(addr.getOrDefault("city", "")))
            .state(String.valueOf(addr.getOrDefault("state", "")))
            .pincode(String.valueOf(addr.getOrDefault("pincode", "")))
            .trackingNumber(String.valueOf(addr.getOrDefault("trackingNumber", "")))
            .courierName(String.valueOf(addr.getOrDefault("courierName", "")))
            .items(safeItems)
            .total(o.getTotal())
            .itemCount(items.size())
            .build();
    }
}