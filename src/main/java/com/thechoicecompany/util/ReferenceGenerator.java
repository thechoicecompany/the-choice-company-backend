package com.thechoicecompany.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class ReferenceGenerator {

    // Inquiry reference: TCC-2026-04271
    public String generateInquiryRef() {
        int year = LocalDate.now().getYear();
        int num  = ThreadLocalRandom.current().nextInt(10000, 99999);
        return String.format("TCC-%d-%05d", year, num);
    }

    // Demo order ID: TCC-DEMO-2026-04271
    public String generateOrderId() {
        int year = LocalDate.now().getYear();
        int num  = ThreadLocalRandom.current().nextInt(10000, 99999);
        return String.format("TCC-DEMO-%d-%05d", year, num);
    }
}
