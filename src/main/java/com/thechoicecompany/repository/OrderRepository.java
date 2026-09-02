package com.thechoicecompany.repository;

import com.thechoicecompany.entity.DemoOrder;
import com.thechoicecompany.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<DemoOrder, Long> {
    Optional<DemoOrder> findByOrderId(String orderId);
    Optional<DemoOrder> findByRazorpayPaymentId(String paymentId);
    boolean existsByRazorpayPaymentId(String paymentId);
    Page<DemoOrder> findByCustomerEmail(String email, Pageable pageable);
    Page<DemoOrder> findByStatus(OrderStatus status, Pageable pageable);
}
