package com.thechoicecompany.entity;
import com.thechoicecompany.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
@Entity
@Table(name = "demo_orders")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DemoOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_id", unique = true, nullable = false, length = 30)
    private String orderId;
    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;
    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;
    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;
    @Column(name = "customer_email", nullable = false, length = 255)
    private String customerEmail;
    @Column(name = "customer_phone", nullable = false, length = 15)
    private String customerPhone;
    // JSONB: { line1, line2, city, state, pincode, country }
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shipping_address", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> shippingAddress;
    // JSONB: [{ productId, name, quantity, samplePrice, subtotal }]
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<Map<String, Object>> items;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;
    @Column(name = "coupon_code", length = 20)
    private String couponCode;
    @Column(name = "gst_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal gstAmount;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private OrderStatus status = OrderStatus.PAID;
    @Column(name = "email_sent")
    @Builder.Default
    private Boolean emailSent = false;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
