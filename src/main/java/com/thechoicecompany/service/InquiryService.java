package com.thechoicecompany.service;

import com.thechoicecompany.dto.request.AddNoteRequest;
import com.thechoicecompany.dto.request.InquiryRequest;
import com.thechoicecompany.dto.request.UpdateInquiryStatusRequest;
import com.thechoicecompany.dto.response.ApiResponse;
import com.thechoicecompany.dto.response.InquiryResponse;
import com.thechoicecompany.dto.response.PagedResponse;
import com.thechoicecompany.entity.Inquiry;
import com.thechoicecompany.entity.InquiryNote;
import com.thechoicecompany.entity.User;
import com.thechoicecompany.enums.InquiryStatus;
import com.thechoicecompany.exception.ResourceNotFoundException;
import com.thechoicecompany.repository.InquiryRepository;
import com.thechoicecompany.repository.UserRepository;
import com.thechoicecompany.util.ReferenceGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final ReferenceGenerator refGenerator;
    private final WhatsAppService whatsAppService;
    private final EmailService emailService;

    // ── Submit new inquiry (public) ────────────────────────────────────────
    @Transactional
    public InquiryResponse submitInquiry(InquiryRequest request) {
        String refNumber = request.getRefNumber() != null
            ? request.getRefNumber()
            : refGenerator.generateInquiryRef();

        Inquiry inquiry = Inquiry.builder()
            .refNumber(refNumber)
            .companyName(request.getCompanyName())
            .contactPerson(request.getContactPerson())
            .designation(request.getDesignation())
            .email(request.getEmail())
            .mobile(request.getMobile())
            .city(request.getCity())
            .state(request.getState())
            .productCategory(request.getProductCategory())
            .quantityRequired(request.getQuantityRequired())
            .budgetRange(request.getBudgetRange())
            .deliveryLocation(request.getDeliveryLocation())
            .brandingRequired(request.getBrandingRequired())
            .packagingRequirement(request.getPackagingRequirement())
            .expectedDeliveryDate(request.getExpectedDeliveryDate())
            .additionalNotes(request.getAdditionalNotes())
            .logoUrl(request.getLogoUrl())
            .source(request.getSource())
            .build();

        Inquiry saved = inquiryRepository.save(inquiry);

        // Fire notifications asynchronously (non-blocking)
        whatsAppService.sendInquiryAlert(saved);
        emailService.sendInquiryAck(saved);
        emailService.sendInquiryInternalAlert(saved);

        log.info("New inquiry saved: {}", refNumber);
        return toResponse(saved);
    }

    // ── List all inquiries with filters (admin) ────────────────────────────
    @Transactional(readOnly = true)
    public PagedResponse<InquiryResponse> listInquiries(
            InquiryStatus status, String state, String category,
            Long assignedTo, int page, int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Inquiry> inquiries = inquiryRepository.findWithFilters(
            status, state, category, assignedTo, pageable
        );
        return PagedResponse.from(inquiries.map(this::toResponse));
    }

    // ── Get single inquiry ─────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public InquiryResponse getInquiry(Long id) {
        Inquiry inquiry = findById(id);
        return toResponse(inquiry);
    }

    // ── Update status ──────────────────────────────────────────────────────
    @Transactional
    public InquiryResponse updateStatus(Long id, UpdateInquiryStatusRequest request) {
        Inquiry inquiry = findById(id);
        inquiry.setStatus(request.getStatus());

        if (request.getAssignedTo() != null) {
            User assignee = userRepository.findById(request.getAssignedTo())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssignedTo()));
            inquiry.setAssignedTo(assignee);
        }

        // Auto-add a note if note text provided
        if (request.getNote() != null && !request.getNote().isBlank()) {
            addNoteInternal(inquiry, request.getNote());
        }

        return toResponse(inquiryRepository.save(inquiry));
    }

    // ── Add note ──────────────────────────────────────────────────────────
    @Transactional
    public InquiryResponse addNote(Long id, AddNoteRequest request) {
        Inquiry inquiry = findById(id);
        addNoteInternal(inquiry, request.getContent());
        return toResponse(inquiryRepository.save(inquiry));
    }

    // ── Dashboard stats ───────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        return Map.of(
            "totalInquiries",    inquiryRepository.count(),
            "newInquiries",      inquiryRepository.countByStatus(InquiryStatus.NEW),
            "convertedInquiries",inquiryRepository.countByStatus(InquiryStatus.CONVERTED),
            "topStates",         inquiryRepository.countByState(),
            "topCategories",     inquiryRepository.countByCategory()
        );
    }

    // ── Private helpers ───────────────────────────────────────────────────
    private Inquiry findById(Long id) {
        return inquiryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Inquiry", "id", id));
    }

    private void addNoteInternal(Inquiry inquiry, String content) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User author  = userRepository.findByEmail(email).orElse(null);
        InquiryNote note = InquiryNote.builder()
            .inquiry(inquiry)
            .content(content)
            .author(author)
            .build();
        inquiry.getNotes().add(note);
    }

    private InquiryResponse toResponse(Inquiry i) {
        return InquiryResponse.builder()
            .id(i.getId())
            .refNumber(i.getRefNumber())
            .companyName(i.getCompanyName())
            .contactPerson(i.getContactPerson())
            .designation(i.getDesignation())
            .email(i.getEmail())
            .mobile(i.getMobile())
            .city(i.getCity())
            .state(i.getState())
            .productCategory(i.getProductCategory())
            .quantityRequired(i.getQuantityRequired())
            .budgetRange(i.getBudgetRange())
            .deliveryLocation(i.getDeliveryLocation())
            .brandingRequired(i.getBrandingRequired())
            .additionalNotes(i.getAdditionalNotes())
            .logoUrl(i.getLogoUrl())
            .source(i.getSource())
            .status(i.getStatus())
            .assignedToName(i.getAssignedTo() != null ? i.getAssignedTo().getFullName() : null)
            .createdAt(i.getCreatedAt())
            .updatedAt(i.getUpdatedAt())
            .build();
    }
}
