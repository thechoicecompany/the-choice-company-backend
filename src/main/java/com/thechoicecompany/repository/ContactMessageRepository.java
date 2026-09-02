package com.thechoicecompany.repository;

import com.thechoicecompany.entity.ContactMessage;
import com.thechoicecompany.enums.ContactStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    // NOTE: mirrors InquiryRepository.findWithFilters — adjust to match
    // however that one is actually written (Specification vs @Query) if it
    // differs, so both stay consistent.
    @Query("""
        SELECT c FROM ContactMessage c
        WHERE (:status IS NULL OR c.status = :status)
        ORDER BY c.createdAt DESC
        """)
    Page<ContactMessage> findWithFilters(@Param("status") ContactStatus status, Pageable pageable);

    long countByStatus(ContactStatus status);
}