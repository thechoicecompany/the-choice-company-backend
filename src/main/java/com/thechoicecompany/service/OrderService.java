package com.thechoicecompany.service;

import com.thechoicecompany.dto.request.DemoOrderRequest;
import com.thechoicecompany.entity.DemoOrder;
import com.thechoicecompany.exception.BusinessException;
import com.thechoicecompany.exception.DuplicateResourceException;
import com.thechoicecompany.repository.OrderRepository;
import com.thechoicecompany.util.ReferenceGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ReferenceGenerator refGenerator;
    private final EmailService emailService;

    @Transactional
    public DemoOrder createOrder(DemoOrderRequest request) {
        // Prevent duplicate payment processing (idempotency)
        if (request.getRazorpayPaymentId() != null &&
            orderRepository.existsByRazorpayPaymentId(request.getRazorpayPaymentId())) {
            throw new DuplicateResourceException(
                "Order already processed for payment: " + request.getRazorpayPaymentId());
        }

        String orderId = request.getOrderId() != null
            ? request.getOrderId()
            : refGenerator.generateOrderId();

        DemoOrder order = DemoOrder.builder()
            .orderId(orderId)
            .razorpayOrderId(request.getRazorpayOrderId())
            .razorpayPaymentId(request.getRazorpayPaymentId())
            .customerName(request.getCustomerName())
            .customerEmail(request.getCustomerEmail())
            .customerPhone(request.getCustomerPhone())
            .shippingAddress(request.getShippingAddress())
            .items(request.getItems())
            .subtotal(request.getSubtotal())
            .discount(request.getDiscount())
            .couponCode(request.getCouponCode())
            .gstAmount(request.getGstAmount())
            .total(request.getTotal())
            .build();

        DemoOrder saved = orderRepository.save(order);

        // Send order confirmation email asynchronously
        emailService.sendOrderConfirmation(saved);

        log.info("Demo order saved: {} (payment: {})", orderId, request.getRazorpayPaymentId());
        return saved;
    }
}
