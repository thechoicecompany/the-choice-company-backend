//package com.thechoicecompany.service;
//
//import com.thechoicecompany.config.AppProperties;
//import com.thechoicecompany.entity.DemoOrder;
//import com.thechoicecompany.entity.Inquiry;
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.thymeleaf.TemplateEngine;
//import org.thymeleaf.context.Context;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class EmailService {
//
//    private final JavaMailSender mailSender;
//    private final TemplateEngine templateEngine;
//    private final AppProperties props;
//
//    // ── Inquiry Acknowledgement Email ──────────────────────────────────────
//    @Async
//    public void sendInquiryAck(Inquiry inquiry) {
//        try {
//            Context ctx = new Context();
//            ctx.setVariable("inquiry", inquiry);
//            ctx.setVariable("refNumber", inquiry.getRefNumber());
//
//            String html = templateEngine.process("email/inquiry-ack", ctx);
//            sendHtmlEmail(
//                inquiry.getEmail(),
//                props.getEmail().getSales(),
//                "Inquiry Received — " + inquiry.getRefNumber() + " | The Choice Company",
//                html
//            );
//            log.info("ACK email sent for inquiry: {}", inquiry.getRefNumber());
//        } catch (Exception e) {
//            log.error("Failed to send inquiry ACK email: {}", e.getMessage());
//        }
//    }
//
//    // ── Order Confirmation Email ───────────────────────────────────────────
//    @Async
//    public void sendOrderConfirmation(DemoOrder order) {
//        try {
//            Context ctx = new Context();
//            ctx.setVariable("order", order);
//
//            String html = templateEngine.process("email/order-confirmation", ctx);
//            sendHtmlEmail(
//                order.getCustomerEmail(),
//                props.getEmail().getSales(),
//                "Order Confirmed — " + order.getOrderId() + " | The Choice Company",
//                html
//            );
//            log.info("Order confirmation email sent: {}", order.getOrderId());
//        } catch (Exception e) {
//            log.error("Failed to send order confirmation email: {}", e.getMessage());
//        }
//    }
//
//    // ── Core Email Sender ──────────────────────────────────────────────────
//    private void sendHtmlEmail(String to, String cc, String subject, String html)
//            throws MessagingException {
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//        helper.setFrom(props.getEmail().getFrom());
//        helper.setTo(to);
//        if (cc != null && !cc.isBlank()) helper.setCc(cc);
//        helper.setSubject(subject);
//        helper.setText(html, true);
//        mailSender.send(message);
//    }
//}



//package com.thechoicecompany.service;
//
//import com.thechoicecompany.config.AppProperties;
//import com.thechoicecompany.entity.DemoOrder;
//import com.thechoicecompany.entity.Inquiry;
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.thymeleaf.TemplateEngine;
//import org.thymeleaf.context.Context;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class EmailService {
//
//    private final JavaMailSender mailSender;
//    private final TemplateEngine templateEngine;
//    private final AppProperties props;
//
//    // ── Inquiry Acknowledgement Email (to customer) ────────────────────────
//    @Async
//    public void sendInquiryAck(Inquiry inquiry) {
//        try {
//            Context ctx = new Context();
//            ctx.setVariable("inquiry", inquiry);
//            ctx.setVariable("refNumber", inquiry.getRefNumber());
//            String html = templateEngine.process("email/inquiry-ack", ctx);
//            sendHtmlEmail(
//                inquiry.getEmail(),
//                null,  // no CC on customer email — internal alert is separate
//                "Inquiry Received — " + inquiry.getRefNumber() + " | The Choice Company",
//                html
//            );
//            log.info("ACK email sent for inquiry: {}", inquiry.getRefNumber());
//        } catch (Exception e) {
//            log.error("Failed to send inquiry ACK email: {}", e.getMessage());
//        }
//    }
//
//    // ── Internal Alert Email (to info@ / your team) ────────────────────────
//    @Async
//    public void sendInquiryInternalAlert(Inquiry inquiry) {
//        try {
//            Context ctx = new Context();
//            ctx.setVariable("inquiry", inquiry);
//            String html = templateEngine.process("email/inquiry-internal-alert", ctx);
//            sendHtmlEmail(
//                props.getEmail().getFrom(),   // TO: info@thechoicecompany.in
//                props.getEmail().getSales(),  // CC: sales@thechoicecompany.in
//                "🔔 New Inquiry: " + inquiry.getRefNumber() + " — " + inquiry.getCompanyName(),
//                html
//            );
//            log.info("Internal alert sent for inquiry: {}", inquiry.getRefNumber());
//        } catch (Exception e) {
//            log.error("Failed to send internal alert email: {}", e.getMessage());
//        }
//    }
//
//    // ── Order Confirmation Email ───────────────────────────────────────────
//    @Async
//    public void sendOrderConfirmation(DemoOrder order) {
//        try {
//            Context ctx = new Context();
//            ctx.setVariable("order", order);
//            String html = templateEngine.process("email/order-confirmation", ctx);
//            sendHtmlEmail(
//                order.getCustomerEmail(),
//                props.getEmail().getSales(),
//                "Order Confirmed — " + order.getOrderId() + " | The Choice Company",
//                html
//            );
//            log.info("Order confirmation email sent: {}", order.getOrderId());
//        } catch (Exception e) {
//            log.error("Failed to send order confirmation email: {}", e.getMessage());
//        }
//    }
//
//    // ── Core Email Sender ──────────────────────────────────────────────────
//    private void sendHtmlEmail(String to, String cc, String subject, String html)
//            throws MessagingException {
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//        helper.setFrom(props.getEmail().getFrom());
//        helper.setTo(to);
//        if (cc != null && !cc.isBlank()) helper.setCc(cc);
//        helper.setSubject(subject);
//        helper.setText(html, true);
//        mailSender.send(message);
//    }
//}
//-------------------------------------

package com.thechoicecompany.service;

import com.thechoicecompany.config.AppProperties;
import com.thechoicecompany.entity.ContactMessage;
import com.thechoicecompany.entity.DemoOrder;
import com.thechoicecompany.entity.Inquiry;
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

