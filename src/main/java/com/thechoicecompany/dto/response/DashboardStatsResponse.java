package com.thechoicecompany.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardStatsResponse {

    // ── INQUIRY STATS ─────────────────────────────────────────
    private Long totalInquiries;
    private Long newInquiries;           // Status = NEW (unread)
    private Long acknowledgedInquiries;
    private Long quoteSentInquiries;
    private Long convertedInquiries;     // Successfully converted to orders
    private Long closedInquiries;

    private Long inquiriesToday;
    private Long inquiriesThisWeek;
    private Long inquiriesThisMonth;

    // Top 5 states by inquiry count
    private List<StatEntry> topStatesByInquiry;

    // Top 5 product categories by inquiry count
    private List<StatEntry> topCategoriesByInquiry;

    // Inquiry source breakdown (website_form, kit_builder, phone etc.)
    private List<StatEntry> inquiryBySource;

    // ── CATALOGUE STATS ───────────────────────────────────────
    private Long totalCatalogueRequests;
    private Long catalogueRequestsToday;
    private Long catalogueRequestsThisMonth;

    // Catalogue request source breakdown
    private List<StatEntry> catalogueBySource;

    // Monthly catalogue download trend (last 6 months)
    private List<MonthlyTrend> catalogueMonthlyTrend;

    // ── INVENTORY STATS ───────────────────────────────────────
    private Long totalProducts;
    private Long activeProducts;
    private Long featuredProducts;
    private Long lowStockProducts;       // Stock <= reorder level
    private Long outOfStockProducts;
    private Long totalStockUnits;

    // ── ORDER STATS ───────────────────────────────────────────
    private Long totalDemoOrders;
    private Long demoOrdersThisMonth;

    // ── GENERIC ENTRIES ───────────────────────────────────────
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class StatEntry {
        private String label;
        private Long count;
        private Double percentage;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MonthlyTrend {
        private String month;    // e.g. "Aug 2026"
        private Long count;
    }
}
