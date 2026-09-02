package com.thechoicecompany.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thechoicecompany.dto.request.InquiryRequest;
import com.thechoicecompany.dto.response.InquiryResponse;
import com.thechoicecompany.enums.InquirySource;
import com.thechoicecompany.enums.InquiryStatus;
import com.thechoicecompany.service.InquiryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InquiryController.class)
@DisplayName("InquiryController Integration Tests")
class InquiryControllerTest {

    @Autowired MockMvc     mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  InquiryService inquiryService;

    @Test
    @DisplayName("POST /api/inquiries — valid request should return 201 with ref number")
    void submitInquiry_ValidRequest_Returns201() throws Exception {
        // Arrange
        InquiryRequest request = buildValidRequest();
        InquiryResponse response = InquiryResponse.builder()
            .id(1L).refNumber("TCC-2026-04271")
            .companyName("Infosys Limited")
            .status(InquiryStatus.NEW)
            .createdAt(LocalDateTime.now())
            .build();

        when(inquiryService.submitInquiry(any(InquiryRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/inquiries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.refNumber").value("TCC-2026-04271"))
            .andExpect(jsonPath("$.message").value("Inquiry submitted successfully"));
    }

    @Test
    @DisplayName("POST /api/inquiries — invalid mobile should return 400")
    void submitInquiry_InvalidMobile_Returns400() throws Exception {
        InquiryRequest request = buildValidRequest();
        request.setMobile("12345"); // Invalid — must be 10 digits starting 6-9

        mockMvc.perform(post("/api/inquiries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/inquiries — quantity below MOQ (50) should return 400")
    void submitInquiry_QuantityBelowMOQ_Returns400() throws Exception {
        InquiryRequest request = buildValidRequest();
        request.setQuantityRequired(10); // Below minimum 50

        mockMvc.perform(post("/api/inquiries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/inquiries — no auth should return 401")
    void listInquiries_NoAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/inquiries"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "SALES_MANAGER")
    @DisplayName("GET /api/inquiries — with SALES_MANAGER role should return 200")
    void listInquiries_WithAuth_Returns200() throws Exception {
        mockMvc.perform(get("/api/inquiries"))
            .andExpect(status().isOk());
    }

    private InquiryRequest buildValidRequest() {
        InquiryRequest r = new InquiryRequest();
        r.setCompanyName("Infosys Limited");
        r.setContactPerson("Rajesh Kumar");
        r.setEmail("rajesh@infosys.com");
        r.setMobile("9876500000");
        r.setCity("Bengaluru");
        r.setState("Karnataka");
        r.setProductCategory("Employee Welcome Kits");
        r.setQuantityRequired(500);
        r.setBudgetRange("500-1000");
        r.setDeliveryLocation("Bengaluru offices");
        r.setSource(InquirySource.WEBSITE_FORM);
        return r;
    }
}
