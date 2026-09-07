package com.thechoicecompany.service;

import com.thechoicecompany.dto.request.UpdateOrderStatusRequest;
import com.thechoicecompany.dto.response.DemoOrderDetailDto;
import com.thechoicecompany.dto.response.DemoOrderSummaryDto;
import com.thechoicecompany.entity.DemoOrder;
import com.thechoicecompany.enums.OrderStatus;
import com.thechoicecompany.exception.ResourceNotFoundException;
import com.thechoicecompany.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final EmailService emailService;
    public Page<DemoOrderSummaryDto> listOrders(Pageable pageable, String status, String search) {
        Page<DemoOrder> orders;

        if (search != null && !search.isBlank()) {
            orders = orderRepository.searchOrders(search.trim(), pageable);
        } else if (status != null && !status.isBlank()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orders = orderRepository.findByStatus(orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid order status filter ignored: {}", status);
                orders = orderRepository.findAll(pageable);
            }
        } else {
            orders = orderRepository.findAll(pageable);
        }

        return orders.map(this::toSummary);
    }

    public DemoOrderDetailDto getOrderDetail(String orderId) {
        DemoOrder order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return toDetail(order);
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private DemoOrderSummaryDto toSummary(DemoOrder o) {
        int itemCount = 0;
        if (o.getItems() instanceof List<?> list) itemCount = list.size();

        return DemoOrderSummaryDto.builder()
            .orderId(o.getOrderId())
            .customerName(o.getCustomerName())
            .customerEmail(o.getCustomerEmail())
            .customerPhone(o.getCustomerPhone())
            .total(o.getTotal())
            .status(o.getStatus())
            .emailSent(o.getEmailSent())
            .createdAt(o.getCreatedAt())
            .itemCount(itemCount)
            .build();
    }
    @Transactional
    public DemoOrderDetailDto updateStatus(String orderId, UpdateOrderStatusRequest request) {
        DemoOrder order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(request.getStatus());

        String trackingNumber = request.getTrackingNumber();

        if (request.getTrackingNumber() != null || request.getCourierName() != null) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> addr =
                order.getShippingAddress() instanceof Map
                    ? new java.util.HashMap<>((Map<String, Object>) order.getShippingAddress())
                    : new java.util.HashMap<>();

            if (request.getTrackingNumber() != null)
                addr.put("trackingNumber", request.getTrackingNumber());
            if (request.getCourierName() != null)
                addr.put("courierName", request.getCourierName());
            if (request.getNote() != null)
                addr.put("statusNote", request.getNote());

            order.setShippingAddress(addr);
        }

        DemoOrder saved = orderRepository.save(order);
        log.info("Order {} status changed: {} → {}", orderId, oldStatus, request.getStatus());

        if (oldStatus != request.getStatus()) {
            emailService.sendOrderStatusUpdate(saved, request.getStatus(), trackingNumber);
        }

        return toDetail(saved);
    }
    
    
    @SuppressWarnings("unchecked")
    private DemoOrderDetailDto toDetail(DemoOrder o) {
        return DemoOrderDetailDto.builder()
            .id(o.getId())
            .orderId(o.getOrderId())
            .razorpayOrderId(o.getRazorpayOrderId())
            .razorpayPaymentId(o.getRazorpayPaymentId())
            .customerName(o.getCustomerName())
            .customerEmail(o.getCustomerEmail())
            .customerPhone(o.getCustomerPhone())
            .shippingAddress(o.getShippingAddress() instanceof Map m ? m : Map.of())
            .items(o.getItems() instanceof List l ? l : List.of())
            .subtotal(o.getSubtotal())
            .discount(o.getDiscount())
            .couponCode(o.getCouponCode())
            .gstAmount(o.getGstAmount())
            .total(o.getTotal())
            .status(o.getStatus())
            .emailSent(o.getEmailSent())
            .createdAt(o.getCreatedAt())
            .updatedAt(o.getUpdatedAt())
            .build();
    }
}