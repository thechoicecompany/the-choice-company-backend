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
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    // ── PUBLIC — called directly by ContactForm.tsx via springApi ──────────
    // No auth required, same as your public inquiry submission endpoint.
    @PostMapping("/api/contact")
    public ResponseEntity<ApiResponse<ContactResponse>> submit(
            @Valid @RequestBody ContactRequest request) {
        ContactResponse response = contactService.submitContact(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Message sent successfully"));
    }

    // ── ADMIN — list, paginated + filterable ────────────────────────────────
    // NOTE: confirm this sits behind whatever secures your existing
    // /api/admin/** routes (Spring Security config / @PreAuthorize) — I'm
    // matching the URL prefix pattern, not re-declaring auth here since I
    // don't have your SecurityConfig file.
    // Wrapped in ApiResponse<> to match adminFetch()'s expected shape —
    // same as fetchInquiries()/fetchAdminProducts() in admin-api.ts, which
    // all unwrap `res.data` after an ApiResponse<PagedResponse<T>>.
    @GetMapping("/api/admin/contact")
    public ResponseEntity<ApiResponse<PagedResponse<ContactResponse>>> list(
            @RequestParam(required = false) ContactStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<ContactResponse> result = contactService.listContactMessages(status, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/api/admin/contact/{id}")
    public ResponseEntity<ApiResponse<ContactResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(contactService.getContactMessage(id)));
    }

    @PatchMapping("/api/admin/contact/{id}/status")
    public ResponseEntity<ApiResponse<ContactResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContactStatusRequest request) {
        ContactResponse response = contactService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Status updated"));
    }
}