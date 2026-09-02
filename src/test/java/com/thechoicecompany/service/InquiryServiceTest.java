package com.thechoicecompany.service;

import com.thechoicecompany.dto.request.InquiryRequest;
import com.thechoicecompany.dto.response.InquiryResponse;
import com.thechoicecompany.entity.Inquiry;
import com.thechoicecompany.enums.InquirySource;
import com.thechoicecompany.enums.InquiryStatus;
import com.thechoicecompany.repository.InquiryRepository;
import com.thechoicecompany.repository.UserRepository;
import com.thechoicecompany.util.ReferenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InquiryService Unit Tests")
class InquiryServiceTest {

    @Mock private InquiryRepository inquiryRepository;
    @Mock private UserRepository    userRepository;
    @Mock private ReferenceGenerator refGenerator;
    @Mock private WhatsAppService   whatsAppService;
    @Mock private EmailService      emailService;

    @InjectMocks
    private InquiryService inquiryService;

    private InquiryRequest validRequest;
    private Inquiry        savedInquiry;

    @BeforeEach
    void setUp() {
        validRequest = new InquiryRequest();
        validRequest.setCompanyName("Infosys Limited");
        validRequest.setContactPerson("Rajesh Kumar");
        validRequest.setDesignation("HR Manager");
        validRequest.setEmail("rajesh@infosys.com");
        validRequest.setMobile("9876500000");
        validRequest.setCity("Bengaluru");
        validRequest.setState("Karnataka");
        validRequest.setProductCategory("Employee Welcome Kits");
        validRequest.setQuantityRequired(500);
        validRequest.setBudgetRange("500-1000");
        validRequest.setDeliveryLocation("Bengaluru — 3 offices");
        validRequest.setBrandingRequired(true);
        validRequest.setSource(InquirySource.WEBSITE_FORM);

        savedInquiry = Inquiry.builder()
            .id(1L)
            .refNumber("TCC-2026-04271")
            .companyName("Infosys Limited")
            .contactPerson("Rajesh Kumar")
            .email("rajesh@infosys.com")
            .mobile("9876500000")
            .city("Bengaluru")
            .state("Karnataka")
            .productCategory("Employee Welcome Kits")
            .quantityRequired(500)
            .budgetRange("500-1000")
            .deliveryLocation("Bengaluru — 3 offices")
            .status(InquiryStatus.NEW)
            .build();
    }

    @Test
    @DisplayName("Should submit inquiry and return response with ref number")
    void submitInquiry_ValidRequest_ShouldReturnResponse() {
        // Arrange
        when(refGenerator.generateInquiryRef()).thenReturn("TCC-2026-04271");
        when(inquiryRepository.save(any(Inquiry.class))).thenReturn(savedInquiry);
        doNothing().when(whatsAppService).sendInquiryAlert(any());
        doNothing().when(emailService).sendInquiryAck(any());

        // Act
        InquiryResponse response = inquiryService.submitInquiry(validRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getRefNumber()).isEqualTo("TCC-2026-04271");
        assertThat(response.getCompanyName()).isEqualTo("Infosys Limited");
        assertThat(response.getStatus()).isEqualTo(InquiryStatus.NEW);

        verify(inquiryRepository, times(1)).save(any(Inquiry.class));
        verify(whatsAppService, times(1)).sendInquiryAlert(any(Inquiry.class));
        verify(emailService, times(1)).sendInquiryAck(any(Inquiry.class));
    }

    @Test
    @DisplayName("Should use provided ref number if already set")
    void submitInquiry_WithPresetRefNumber_ShouldNotGenerateNew() {
        // Arrange
        validRequest.setRefNumber("TCC-2026-99999");
        when(inquiryRepository.save(any(Inquiry.class))).thenReturn(savedInquiry);

        // Act
        inquiryService.submitInquiry(validRequest);

        // Assert — refGenerator should NOT be called
        verify(refGenerator, never()).generateInquiryRef();
    }

    @Test
    @DisplayName("Should still return response even if WhatsApp fails (async)")
    void submitInquiry_WhatsAppFails_ShouldStillSucceed() {
        // Arrange
        when(refGenerator.generateInquiryRef()).thenReturn("TCC-2026-04271");
        when(inquiryRepository.save(any(Inquiry.class))).thenReturn(savedInquiry);
        doThrow(new RuntimeException("WhatsApp timeout"))
            .when(whatsAppService).sendInquiryAlert(any());

        // Act — should NOT throw despite WhatsApp failure
        InquiryResponse response = inquiryService.submitInquiry(validRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getRefNumber()).isEqualTo("TCC-2026-04271");
    }
}
