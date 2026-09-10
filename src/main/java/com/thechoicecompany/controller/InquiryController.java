package com.thechoicecompany.controller;

import com.thechoicecompany.dto.request.AddNoteRequest;
import com.thechoicecompany.dto.request.InquiryRequest;
import com.thechoicecompany.dto.request.UpdateInquiryStatusRequest;
import com.thechoicecompany.dto.response.ApiResponse;
import com.thechoicecompany.dto.response.InquiryResponse;
import com.thechoicecompany.dto.response.PagedResponse;
import com.thechoicecompany.enums.InquiryStatus;
import com.thechoicecompany.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
@Tag(name = "Inquiries", description = "Bulk gifting inquiry management")
public class InquiryController {

    private final InquiryService inquiryService;

    // ── PUBLIC — called by Next.js /api/inquiry Route Handler ──────────────
    @PostMapping
    @Operation(summary = "Submit a new bulk inquiry (public)")
    public ResponseEntity<ApiResponse<InquiryResponse>> submit(
            @Valid @RequestBody InquiryRequest request) {
        InquiryResponse response = inquiryService.submitInquiry(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Inquiry submitted successfully"));
    }

    // ── ADMIN — JWT Required ───────────────────────────────────────────────
    @GetMapping
    @Operation(summary = "List all inquiries with filters", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER','PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<InquiryResponse>>> list(
            @RequestParam(required = false) InquiryStatus status,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long assignedTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
            inquiryService.listInquiries(status, state, category, assignedTo, page, size)
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inquiry by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER','PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<InquiryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(inquiryService.getInquiry(id)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update inquiry status", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER','PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<InquiryResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInquiryStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            inquiryService.updateStatus(id, request), "Status updated"
        ));
    }

    @PostMapping("/{id}/notes")
    @Operation(summary = "Add follow-up note to inquiry", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER','PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<InquiryResponse>> addNote(
            @PathVariable Long id,
            @Valid @RequestBody AddNoteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            inquiryService.addNote(id, request), "Note added"
        ));
    }

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get dashboard statistics", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER','PRODUCT_MANAGER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboardStats() {
        return ResponseEntity.ok(ApiResponse.success(inquiryService.getDashboardStats()));
    }
}
