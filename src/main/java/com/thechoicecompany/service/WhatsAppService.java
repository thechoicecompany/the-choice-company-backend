package com.thechoicecompany.service;

import com.thechoicecompany.config.AppProperties;
import com.thechoicecompany.entity.Inquiry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppService {

    private final AppProperties props;
    private final OkHttpClient httpClient = new OkHttpClient();

    @Async
    public void sendInquiryAlert(Inquiry inquiry) {
        String token   = props.getWhatsapp().getToken();
        String phoneId = props.getWhatsapp().getPhoneId();
        String salesNo = props.getWhatsapp().getSalesNumber();

        if (token == null || token.isBlank()) {
            log.warn("WhatsApp credentials not configured — skipping alert");
            return;
        }

        String message = String.format(
            "🎁 *New Inquiry — %s*\n\n" +
            "*Company:*  %s\n" +
            "*Contact:*  %s (%s)\n" +
            "*Mobile:*   %s\n" +
            "*Category:* %s\n" +
            "*Quantity:* %d units\n" +
            "*Budget:*   %s\n" +
            "*Location:* %s, %s\n" +
            "*Branding:* %s\n\n" +
            "Reply here to follow up immediately.",
            inquiry.getRefNumber(),
            inquiry.getCompanyName(),
            inquiry.getContactPerson(),
            inquiry.getDesignation() != null ? inquiry.getDesignation() : "N/A",
            inquiry.getMobile(),
            inquiry.getProductCategory(),
            inquiry.getQuantityRequired(),
            inquiry.getBudgetRange(),
            inquiry.getCity(), inquiry.getState(),
            Boolean.TRUE.equals(inquiry.getBrandingRequired()) ? "Yes" : "No"
        );

        String json = String.format("""
            {
              "messaging_product": "whatsapp",
              "to": "%s",
              "type": "text",
              "text": { "body": "%s" }
            }
            """, salesNo, message.replace("\"", "\\\"").replace("\n", "\\n"));

        Request request = new Request.Builder()
            .url("https://graph.facebook.com/v18.0/" + phoneId + "/messages")
            .addHeader("Authorization", "Bearer " + token)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(json, MediaType.parse("application/json")))
            .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("WhatsApp alert failed: {} {}", response.code(), response.message());
            } else {
                log.info("WhatsApp alert sent for inquiry: {}", inquiry.getRefNumber());
            }
        } catch (Exception e) {
            log.error("WhatsApp alert exception: {}", e.getMessage());
        }
    }
}
