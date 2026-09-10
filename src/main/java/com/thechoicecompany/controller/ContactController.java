package com.thechoicecompany.controller;

import com.thechoicecompany.dto.request.ContactRequest;
import com.thechoicecompany.dto.request.UpdateContactStatusRequest;
import com.thechoicecompany.dto.response.ApiResponse;
import com.thechoicecompany.dto.response.ContactResponse;
import com.thechoicecompany.dto.response.PagedResponse;
import com.thechoicecompany.enums.ContactStatus;
import com.thechoicecompany.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    // ── PUBLIC ──────────────────────────────────────────────────────────────
    @PostMapping("/api/contact")
    public ResponseEntity<ApiResponse<ContactResponse>> submit(
            @Valid @RequestBody ContactRequest request) {
        ContactResponse response = contactService.submitContact(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Message sent successfully"));
    }

    // ── ADMIN — role guard added ─────────────────────────────────────────────
    @GetMapping("/api/admin/contact")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER','SALES_EXECUTIVE')")
    public ResponseEntity<ApiResponse<PagedResponse<ContactResponse>>> list(
            @RequestParam(required = false) ContactStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
            contactService.listContactMessages(status, page, size)));
    }

    @GetMapping("/api/admin/contact/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER','SALES_EXECUTIVE')")
    public ResponseEntity<ApiResponse<ContactResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(contactService.getContactMessage(id)));
    }

    @PatchMapping("/api/admin/contact/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER')")
    public ResponseEntity<ApiResponse<ContactResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContactStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            contactService.updateStatus(id, request), "Status updated"));
    }
}