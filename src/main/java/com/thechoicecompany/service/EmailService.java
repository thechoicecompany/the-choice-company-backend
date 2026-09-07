package com.thechoicecompany.service;

import com.thechoicecompany.config.AppProperties;
import com.thechoicecompany.entity.CatalogueRequest;
import com.thechoicecompany.entity.ContactMessage;
import com.thechoicecompany.entity.DemoOrder;
import com.thechoicecompany.entity.Inquiry;
import com.thechoicecompany.enums.OrderStatus;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
	// EmailService fields — add:
	private final com.thechoicecompany.repository.OrderRepository orderRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final AppProperties props;

    // ── Inquiry Acknowledgement Email (to customer) ────────────────────────
    @Async
    public void sendInquiryAck(Inquiry inquiry) {
        try {
            Context ctx = new Context();
            ctx.setVariable("inquiry", inquiry);
            ctx.setVariable("refNumber", inquiry.getRefNumber());
            String html = templateEngine.process("email/inquiry-ack", ctx);
            sendHtmlEmail(
                inquiry.getEmail(),
                null,  // no CC on customer email — internal alert is separate
                "Inquiry Received — " + inquiry.getRefNumber() + " | The Choice Company",
                html
            );
            log.info("ACK email sent for inquiry: {}", inquiry.getRefNumber());
        } catch (Exception e) {
            log.error("Failed to send inquiry ACK email: {}", e.getMessage());
        }
    }

    // ── Internal Alert Email (to info@ / your team) ────────────────────────
    @Async
    public void sendInquiryInternalAlert(Inquiry inquiry) {
        try {
            Context ctx = new Context();
            ctx.setVariable("inquiry", inquiry);
            String html = templateEngine.process("email/inquiry-internal-alert", ctx);
            sendHtmlEmail(
                props.getEmail().getFrom(),   // TO: info@thechoicecompany.in
                props.getEmail().getSales(),  // CC: sales@thechoicecompany.in
                "🔔 New Inquiry: " + inquiry.getRefNumber() + " — " + inquiry.getCompanyName(),
                html
            );
            log.info("Internal alert sent for inquiry: {}", inquiry.getRefNumber());
        } catch (Exception e) {
            log.error("Failed to send internal alert email: {}", e.getMessage());
        }
    }

    // ── Contact Form Acknowledgement Email (to customer) ────────────────────
    // NEW — mirrors sendInquiryAck exactly, just a different template/entity.
    @Async
    public void sendContactAck(ContactMessage message) {
        try {
            Context ctx = new Context();
            ctx.setVariable("contact", message);
            String html = templateEngine.process("email/contact-ack", ctx);
            sendHtmlEmail(
                message.getEmail(),
                null,
                "We've received your message | The Choice Company",
                html
            );
            log.info("Contact ACK email sent to: {}", message.getEmail());
        } catch (Exception e) {
            log.error("Failed to send contact ACK email: {}", e.getMessage());
        }
    }

    // ── Contact Form Internal Alert Email (to info@ / your team) ────────────
    // NEW — this is the "we get a Gmail when someone submits" requirement.
    @Async
    public void sendContactInternalAlert(ContactMessage message) {
        try {
            Context ctx = new Context();
            ctx.setVariable("contact", message);
            String html = templateEngine.process("email/contact-internal-alert", ctx);
            sendHtmlEmail(
                props.getEmail().getFrom(),   // TO: info@thechoicecompany.in
                props.getEmail().getSales(),  // CC: sales@thechoicecompany.in
                "📩 New Contact Message: " + message.getSubject() + " — " + message.getName(),
                html
            );
            log.info("Contact internal alert sent for message id={}", message.getId());
        } catch (Exception e) {
            log.error("Failed to send contact internal alert email: {}", e.getMessage());
        }
    }

    // ── Order Confirmation Email ───────────────────────────────────────────
    @Async
    public void sendOrderConfirmation(DemoOrder order) {
        try {
            Context ctx = new Context();
            ctx.setVariable("order", order);
            String html = templateEngine.process("email/order-confirmation", ctx);
            sendHtmlEmail(
                order.getCustomerEmail(),
                props.getEmail().getSales(),
                "Order Confirmed — " + order.getOrderId() + " | The Choice Company",
                html
            );
            log.info("Order confirmation email sent: {}", order.getOrderId());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email: {}", e.getMessage());
        }
    }
 // ── Catalogue Ack Email (to customer, with download link) ──────────────
    @Async
    public void sendCatalogueAck(CatalogueRequest request, String downloadUrl) {
        try {
            Context ctx = new Context();
            ctx.setVariable("catalogue", request);
            ctx.setVariable("downloadUrl", downloadUrl);
            String html = templateEngine.process("email/catalogue-ack", ctx);
            sendHtmlEmail(
                request.getEmail(),
                null,
                "Your Catalogue is Ready | The Choice Company",
                html
            );
            log.info("Catalogue ACK email sent to: {}", request.getEmail());
        } catch (Exception e) {
            log.error("Failed to send catalogue ACK email: {}", e.getMessage());
        }
    }

 // In EmailService.java — add this method
 // Separate @Transactional so the main order save is never blocked

 @Async
 @org.springframework.transaction.annotation.Transactional
 public void sendOrderConfirmationAndFlag(Long orderId) {
     // Import your OrderRepository here via constructor injection
     com.thechoicecompany.entity.DemoOrder order = orderRepository.findById(orderId)
         .orElse(null);
     if (order == null) {
         log.error("sendOrderConfirmationAndFlag: order not found id={}", orderId);
         return;
     }
     try {
         sendOrderConfirmation(order);  // existing method — uses Thymeleaf template
         order.setEmailSent(true);
         orderRepository.save(order);
     } catch (Exception e) {
         log.error("Order confirmation email failed for {}: {}", order.getOrderId(), e.getMessage());
         // emailSent stays false — you can query these and retry
     }
 }
    
    
    // ── Catalogue Internal Alert (to info@ / sales) ──────────────────────────
    @Async
    public void sendCatalogueInternalAlert(CatalogueRequest request) {
        try {
            Context ctx = new Context();
            ctx.setVariable("catalogue", request);
            String html = templateEngine.process("email/catalogue-internal-alert", ctx);
            sendHtmlEmail(
                props.getEmail().getFrom(),
                props.getEmail().getSales(),
                "📥 New Catalogue Request: " + request.getEmail() + " via " + request.getSource(),
                html
            );
            log.info("Catalogue internal alert sent for request id={}", request.getId());
        } catch (Exception e) {
            log.error("Failed to send catalogue internal alert email: {}", e.getMessage());
        }
    }
    // ── Order Status Update Email (to customer) ─────────────────────────────
    private static final java.util.Set<OrderStatus> NOTIFIABLE_STATUSES = java.util.Set.of(
        OrderStatus.SHIPPED, OrderStatus.DELIVERED, OrderStatus.CANCELLED, OrderStatus.REFUNDED
    );

    @Async
    public void sendOrderStatusUpdate(DemoOrder order, OrderStatus newStatus, String trackingNumber) {
        if (!NOTIFIABLE_STATUSES.contains(newStatus)) return;

        try {
            String statusLabel;
            String headline;
            String message;

            switch (newStatus) {
                case SHIPPED -> {
                    statusLabel = "ORDER SHIPPED 📦";
                    headline = "Your sample is on its way!";
                    message = "Your order has been shipped and is on its way to you.";
                }
                case DELIVERED -> {
                    statusLabel = "ORDER DELIVERED ✓";
                    headline = "Your sample has arrived!";
                    message = "Your order has been marked as delivered. We hope you love it.";
                }
                case CANCELLED -> {
                    statusLabel = "ORDER CANCELLED";
                    headline = "Your order has been cancelled";
                    message = "Your order has been cancelled. If you have any questions, please reach out to us.";
                }
                case REFUNDED -> {
                    statusLabel = "ORDER REFUNDED";
                    headline = "Your refund has been processed";
                    message = "Your order has been refunded. It may take a few business days to reflect in your account.";
                }
                default -> { return; }
            }

            Context ctx = new Context();
            ctx.setVariable("order", order);
            ctx.setVariable("statusLabel", statusLabel);
            ctx.setVariable("headline", headline);
            ctx.setVariable("message", message);
            ctx.setVariable("trackingNumber", trackingNumber);

            String html = templateEngine.process("email/order-status-update", ctx);
            sendHtmlEmail(
                order.getCustomerEmail(),
                null,
                statusLabel + " — " + order.getOrderId() + " | The Choice Company",
                html
            );
            log.info("Status update email sent: {} ({})", order.getOrderId(), newStatus);
        } catch (Exception e) {
            log.error("Failed to send status update email for {}: {}", order.getOrderId(), e.getMessage());
        }
    }
    // ── Core Email Sender ──────────────────────────────────────────────────
    private void sendHtmlEmail(String to, String cc, String subject, String html)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(props.getEmail().getFrom());
        helper.setTo(to);
        if (cc != null && !cc.isBlank()) helper.setCc(cc);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }
}

