package com.thechoicecompany.repository;

import com.thechoicecompany.entity.Inquiry;
import com.thechoicecompany.enums.InquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    Optional<Inquiry> findByRefNumber(String refNumber);

    Page<Inquiry> findByStatus(InquiryStatus status, Pageable pageable);

    Page<Inquiry> findByAssignedToId(Long userId, Pageable pageable);

    @Query("SELECT i FROM Inquiry i WHERE " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:state IS NULL OR i.state = :state) AND " +
           "(:category IS NULL OR i.productCategory = :category) AND " +
           "(:assignedTo IS NULL OR i.assignedTo.id = :assignedTo)")
    Page<Inquiry> findWithFilters(
        @Param("status")     InquiryStatus status,
        @Param("state")      String state,
        @Param("category")   String category,
        @Param("assignedTo") Long assignedTo,
        Pageable pageable
    );

    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.status = :status")
    Long countByStatus(@Param("status") InquiryStatus status);

    @Query("SELECT i.state, COUNT(i) FROM Inquiry i GROUP BY i.state ORDER BY COUNT(i) DESC")
    List<Object[]> countByState();

    @Query("SELECT i.productCategory, COUNT(i) FROM Inquiry i GROUP BY i.productCategory ORDER BY COUNT(i) DESC")
    List<Object[]> countByCategory();

    List<Inquiry> findByEmailSentFalseAndCreatedAtAfter(LocalDateTime since);

    @Query(value = """
        SELECT
            COUNT(*)                                                AS total,
            COUNT(*) FILTER (WHERE status = 'NEW')                 AS new_count,
            COUNT(*) FILTER (WHERE status = 'ACKNOWLEDGED')        AS ack_count,
            COUNT(*) FILTER (WHERE status = 'QUOTE_SENT')          AS quote_sent_count,
            COUNT(*) FILTER (WHERE status = 'CONVERTED')           AS converted_count,
            COUNT(*) FILTER (WHERE status = 'CLOSED')              AS closed_count,
            COUNT(*) FILTER (WHERE created_at >= :today)           AS today_count,
            COUNT(*) FILTER (WHERE created_at >= :weekStart)       AS week_count,
            COUNT(*) FILTER (WHERE created_at >= :monthStart)      AS month_count
        FROM inquiries
        """, nativeQuery = true)
    Object[] getInquirySummary(
        @Param("today")      LocalDateTime today,
        @Param("weekStart")  LocalDateTime weekStart,
        @Param("monthStart") LocalDateTime monthStart
    );
}