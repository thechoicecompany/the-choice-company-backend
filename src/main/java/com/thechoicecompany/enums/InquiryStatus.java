package com.thechoicecompany.enums;

public enum InquiryStatus {
    NEW,            // Just submitted — no action taken yet
    ACKNOWLEDGED,   // Sales team has seen it — ACK sent
    QUOTE_SENT,     // Quotation emailed to client
    FOLLOW_UP,      // Follow-up in progress
    CONVERTED,      // Successfully became a bulk order
    CLOSED          // Lost or not interested
}
