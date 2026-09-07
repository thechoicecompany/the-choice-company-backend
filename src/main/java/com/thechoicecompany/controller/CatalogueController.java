package com.thechoicecompany.controller;

import com.thechoicecompany.dto.request.CatalogueRequestDto;
import com.thechoicecompany.dto.response.ApiResponse;
import com.thechoicecompany.dto.response.CatalogueResponseDto;
import com.thechoicecompany.dto.response.PagedResponse;
import com.thechoicecompany.entity.CatalogueRequest;
import com.thechoicecompany.exception.BusinessException;
import com.thechoicecompany.repository.CatalogueRequestRepository;
import com.thechoicecompany.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/catalogue")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Catalogue", description = "Catalogue download tracking and admin reporting")
public class CatalogueController {

    private final CatalogueRequestRepository catalogueRepository;
    private final EmailService emailService;

    // Cloudinary raw URL — same PDF for every request, no per-request upload needed
    @Value("${app.catalogue.download-url}")
    private String catalogueDownloadUrl;

    // ── PUBLIC — exit-intent popup, static catalog page, AND the gallery
    //             contact-gate (CatalogGateModal) all hit this ───────────────
    @PostMapping("/request")
    @Operation(summary = "Track a catalogue download request (public)")
    public ResponseEntity<ApiResponse<CatalogueResponseDto>> requestCatalogue(
            @Valid @RequestBody CatalogueRequestDto dto,
            HttpServletRequest httpRequest) {

        boolean recentRequest = catalogueRepository.existsByEmailAndCreatedAtAfter(
            dto.getEmail(), LocalDateTime.now().minusHours(1));
        if (recentRequest) {
            throw new BusinessException("Catalogue already sent to this email recently. Check your inbox.");
        }

        String ipAddress = httpRequest.getHeader("X-Forwarded-For");
        if (ipAddress == null) ipAddress = httpRequest.getRemoteAddr();

        CatalogueRequest request = CatalogueRequest.builder()
            .email(dto.getEmail())
            .companyName(dto.getCompanyName())
            .phone(dto.getPhone())
            .source(dto.getSource() != null ? dto.getSource() : "exit_popup")
            .pageUrl(dto.getPageUrl())
            .ipAddress(ipAddress)
            .emailSent(true)
            .build();
        catalogueRepository.save(request);
        log.info("Catalogue request saved: {} via {}", dto.getEmail(), dto.getSource());

        emailService.sendCatalogueAck(request, catalogueDownloadUrl);
        emailService.sendCatalogueInternalAlert(request);

        return ResponseEntity.ok(ApiResponse.success(
            new CatalogueResponseDto(catalogueDownloadUrl),
            "Catalogue ready"
        ));
    }

    // ── ADMIN — unchanged ───────────────────────────────────────────────────
    @GetMapping("/admin/requests")
    @Operation(summary = "List all catalogue requests (admin)")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SALES_MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<CatalogueRequest>>> listRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CatalogueRequest> requests = catalogueRepository.findAllByOrderByCreatedAtDesc(
            PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(requests)));
    }
}