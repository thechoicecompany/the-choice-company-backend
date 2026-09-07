package com.thechoicecompany.repository;

import com.thechoicecompany.entity.DemoOrder;
import com.thechoicecompany.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<DemoOrder, Long> {

    Optional<DemoOrder> findByOrderId(String orderId);
    Optional<DemoOrder> findByRazorpayPaymentId(String paymentId);
    boolean existsByRazorpayPaymentId(String paymentId);
    Page<DemoOrder> findByCustomerEmail(String email, Pageable pageable);
    Page<DemoOrder> findByStatus(OrderStatus status, Pageable pageable);
    long countByCreatedAtAfter(LocalDateTime date);  // ← added

    @Query("""
            SELECT o FROM DemoOrder o
            WHERE LOWER(o.customerName)  LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(o.customerEmail) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(o.orderId)       LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<DemoOrder> searchOrders(@Param("q") String query, Pageable pageable);
}