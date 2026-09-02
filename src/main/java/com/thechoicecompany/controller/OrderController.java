package com.thechoicecompany.controller;

import com.thechoicecompany.dto.request.DemoOrderRequest;
import com.thechoicecompany.dto.response.ApiResponse;
import com.thechoicecompany.entity.DemoOrder;
import com.thechoicecompany.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo-orders")
@RequiredArgsConstructor
@Tag(name = "Demo Orders", description = "Sample purchase orders from Razorpay checkout")
public class OrderController {

    private final OrderService orderService;

    // Called by Next.js /api/razorpay/verify-payment after HMAC verification
    @PostMapping
    @Operation(summary = "Save verified demo order (called by Next.js after payment verification)")
    public ResponseEntity<ApiResponse<DemoOrder>> createOrder(
            @Valid @RequestBody DemoOrderRequest request) {
        DemoOrder order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(order, "Order saved successfully"));
    }
}
