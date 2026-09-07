package com.thechoicecompany.controller;

import com.thechoicecompany.dto.request.UpdateOrderStatusRequest;
import com.thechoicecompany.dto.response.ApiResponse;
import com.thechoicecompany.dto.response.DemoOrderDetailDto;
import com.thechoicecompany.dto.response.DemoOrderSummaryDto;
import com.thechoicecompany.service.AdminOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/demo-orders")
@RequiredArgsConstructor
@Tag(name = "Admin — Demo Orders", description = "Order management for admin panel")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    // SUPER_ADMIN, SALES_MANAGER, SALES_EXECUTIVE can view
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER','SALES_EXECUTIVE')")
    @Operation(summary = "List all demo orders — paginated, filterable")
    public ResponseEntity<ApiResponse<Page<DemoOrderSummaryDto>>> listOrders(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "20")  int size,
            @RequestParam(required = false)     String status,
            @RequestParam(required = false)     String search  // name/email/orderId
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<DemoOrderSummaryDto> result = adminOrderService.listOrders(pageable, status, search);
        return ResponseEntity.ok(ApiResponse.success(result, "Orders fetched"));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER','SALES_EXECUTIVE')")
    @Operation(summary = "Get full order detail")
    public ResponseEntity<ApiResponse<DemoOrderDetailDto>> getOrder(
            @PathVariable String orderId) {
        return ResponseEntity.ok(
            ApiResponse.success(adminOrderService.getOrderDetail(orderId), "Order fetched")
        );
    }

    // Only SUPER_ADMIN and SALES_MANAGER can update status
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER')")
    @Operation(summary = "Update order status and tracking info")
    public ResponseEntity<ApiResponse<DemoOrderDetailDto>> updateStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(
            ApiResponse.success(adminOrderService.updateStatus(orderId, request), "Status updated")
        );
    }
}