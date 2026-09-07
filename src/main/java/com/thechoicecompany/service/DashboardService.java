package com.thechoicecompany.service;

import com.thechoicecompany.dto.response.DashboardStatsResponse;
import com.thechoicecompany.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final InquiryRepository          inquiryRepository;
    private final CatalogueRequestRepository catalogueRepository;
    private final ProductRepository          productRepository;
    private final InventoryRepository        inventoryRepository;
    private final OrderRepository            orderRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getFullStats() {
        LocalDateTime now          = LocalDateTime.now();
        LocalDateTime today        = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart    = now.minusDays(7);
        LocalDateTime monthStart   = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime sixMonthsAgo = now.minusMonths(6);

        // ── Inquiry stats ──────────────────────────────────────
        Object[] raw = inquiryRepository.getInquirySummary(today, weekStart, monthStart);
        Object[] inqSummary = (raw.length == 1 && raw[0] instanceof Object[])
            ? (Object[]) raw[0]
            : raw;

        long totalInquiries = toLong(inqSummary[0]);
        long newInq         = toLong(inqSummary[1]);
        long ackInq         = toLong(inqSummary[2]);
        long quoteSentInq   = toLong(inqSummary[3]);
        long convertedInq   = toLong(inqSummary[4]);
        long closedInq      = toLong(inqSummary[5]);
        long inquiriesToday = toLong(inqSummary[6]);
        long inquiriesWeek  = toLong(inqSummary[7]);
        long inquiriesMonth = toLong(inqSummary[8]);

        List<Object[]> stateData    = inquiryRepository.countByState();
        List<Object[]> categoryData = inquiryRepository.countByCategory();

        List<DashboardStatsResponse.StatEntry> topStates = stateData.stream()
            .limit(5)
            .map(row -> DashboardStatsResponse.StatEntry.builder()
                .label((String) row[0])
                .count(toLong(row[1]))
                .percentage(totalInquiries > 0 ? (toLong(row[1]) * 100.0 / totalInquiries) : 0)
                .build())
            .collect(Collectors.toList());

        List<DashboardStatsResponse.StatEntry> topCategories = categoryData.stream()
            .limit(5)
            .map(row -> DashboardStatsResponse.StatEntry.builder()
                .label((String) row[0])
                .count(toLong(row[1]))
                .percentage(totalInquiries > 0 ? (toLong(row[1]) * 100.0 / totalInquiries) : 0)
                .build())
            .collect(Collectors.toList());

        // ── Catalogue stats ────────────────────────────────────
        long totalCatalogue = catalogueRepository.count();
        long catalogueToday = catalogueRepository.countByCreatedAtAfter(today);
        long catalogueMonth = catalogueRepository.countByCreatedAtAfter(monthStart);

        List<Object[]> catSourceData = catalogueRepository.countBySourceGrouped();
        List<DashboardStatsResponse.StatEntry> catalogueBySource = catSourceData.stream()
            .map(row -> DashboardStatsResponse.StatEntry.builder()
                .label((String) row[0])
                .count(toLong(row[1]))
                .percentage(totalCatalogue > 0 ? (toLong(row[1]) * 100.0 / totalCatalogue) : 0)
                .build())
            .collect(Collectors.toList());

        List<Object[]> monthlyRaw = catalogueRepository.monthlyTrend(sixMonthsAgo);
        List<DashboardStatsResponse.MonthlyTrend> monthlyTrend = monthlyRaw.stream()
            .map(row -> DashboardStatsResponse.MonthlyTrend.builder()
                .month(row[0].toString())
                .count(((Number) row[1]).longValue())
                .build())
            .collect(Collectors.toList());

        // ── Inventory stats ────────────────────────────────────
        long totalProducts  = productRepository.count();
        long activeProducts = productRepository
            .findWithFilters(null, null, null, null, null, null, PageRequest.of(0, 1))
            .getTotalElements();
        long lowStock   = inventoryRepository.countLowStockItems();
        long outOfStock = inventoryRepository.countOutOfStockItems();
        Long totalUnits = inventoryRepository.getTotalStockUnits();

        // ── Order stats ────────────────────────────────────────
        long totalOrders     = orderRepository.count();
        long ordersThisMonth = orderRepository.countByCreatedAtAfter(monthStart);

        return DashboardStatsResponse.builder()
            .totalInquiries(totalInquiries)
            .newInquiries(newInq)
            .acknowledgedInquiries(ackInq)
            .quoteSentInquiries(quoteSentInq)
            .convertedInquiries(convertedInq)
            .closedInquiries(closedInq)
            .inquiriesToday(inquiriesToday)
            .inquiriesThisWeek(inquiriesWeek)
            .inquiriesThisMonth(inquiriesMonth)
            .topStatesByInquiry(topStates)
            .topCategoriesByInquiry(topCategories)
            .totalCatalogueRequests(totalCatalogue)
            .catalogueRequestsToday(catalogueToday)
            .catalogueRequestsThisMonth(catalogueMonth)
            .catalogueBySource(catalogueBySource)
            .catalogueMonthlyTrend(monthlyTrend)
            .totalProducts(totalProducts)
            .activeProducts(activeProducts)
            .lowStockProducts(lowStock)
            .outOfStockProducts(outOfStock)
            .totalStockUnits(totalUnits != null ? totalUnits : 0L)
            .totalDemoOrders(totalOrders)
            .demoOrdersThisMonth(ordersThisMonth)
            .build();
    }

    // ── Helper: native queries return BigInteger/BigDecimal, not Long ──────
    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Long l)       return l;
        if (value instanceof BigInteger bi) return bi.longValue();
        if (value instanceof Number n)     return n.longValue();
        return 0L;
    }
}