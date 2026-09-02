package com.thechoicecompany.service;

import com.thechoicecompany.dto.request.ContactRequest;
import com.thechoicecompany.dto.request.UpdateContactStatusRequest;
import com.thechoicecompany.dto.response.ContactResponse;
import com.thechoicecompany.dto.response.PagedResponse;
import com.thechoicecompany.entity.ContactMessage;
import com.thechoicecompany.entity.User;
import com.thechoicecompany.enums.ContactStatus;
import com.thechoicecompany.exception.ResourceNotFoundException;
import com.thechoicecompany.repository.ContactMessageRepository;
import com.thechoicecompany.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;
    private final UserRepository userRepository;
    private final EmailService emailService; // reused — same bean InquiryService uses

    // ── Submit new contact message (public) ─────────────────────────────────
    @Transactional
    public ContactResponse submitContact(ContactRequest request) {
        ContactMessage message = ContactMessage.builder()
            .name(request.getName())
            .company(request.getCompany())
            .email(request.getEmail())
            .phone(request.getPhone())
            .subject(request.getSubject())
            .message(request.getMessage())
            .status(ContactStatus.NEW)
            .build();

        ContactMessage saved = contactMessageRepository.save(message);

        // Reuse EmailService — same JavaMailSender/Thymeleaf setup as inquiries,
        // just new templates + methods (see EmailService edits below).
        emailService.sendContactAck(saved);
        emailService.sendContactInternalAlert(saved);

        log.info("New contact message saved: id={}, subject={}", saved.getId(), saved.getSubject());
        return toResponse(saved);
    }

    // ── List all messages with filters (admin) ──────────────────────────────
    @Transactional(readOnly = true)
    public PagedResponse<ContactResponse> listContactMessages(
            ContactStatus status, int page, int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ContactMessage> messages = contactMessageRepository.findWithFilters(status, pageable);
        return PagedResponse.from(messages.map(this::toResponse));
    }

    // ── Get single message ───────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ContactResponse getContactMessage(Long id) {
        return toResponse(findById(id));
    }

    // ── Update status / assign (admin) ───────────────────────────────────────
    @Transactional
    public ContactResponse updateStatus(Long id, UpdateContactStatusRequest request) {
        ContactMessage message = findById(id);
        message.setStatus(request.getStatus());

        if (request.getAssignedTo() != null) {
            User assignee = userRepository.findById(request.getAssignedTo())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssignedTo()));
            message.setAssignedTo(assignee);
        }

        return toResponse(contactMessageRepository.save(message));
    }

    // ── Dashboard stat (optional — plug into existing admin dashboard) ───────
    @Transactional(readOnly = true)
    public long countNew() {
        return contactMessageRepository.countByStatus(ContactStatus.NEW);
    }

    // ── Private helpers ───────────────────────────────────────────────────
    private ContactMessage findById(Long id) {
        return contactMessageRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ContactMessage", "id", id));
    }

    private ContactResponse toResponse(ContactMessage c) {
        return ContactResponse.builder()
            .id(c.getId())
            .name(c.getName())
            .company(c.getCompany())
            .email(c.getEmail())
            .phone(c.getPhone())
            .subject(c.getSubject())
            .message(c.getMessage())
            .status(c.getStatus())
            .assignedToName(c.getAssignedTo() != null ? c.getAssignedTo().getFullName() : null)
            .createdAt(c.getCreatedAt())
            .updatedAt(c.getUpdatedAt())
            .build();
    }
}