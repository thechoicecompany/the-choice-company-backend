package com.thechoicecompany.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds one Caffeine cache of buckets per rule name (login, inquiries, etc).
 * Buckets auto-expire 10 minutes after last access, so keys that key by
 * a dynamic value (e.g. orderId) don't accumulate forever in memory.
 */
@Component
public class RateLimiterService {

    private final Map<String, Cache<String, Bucket>> cachesByRule = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String ruleName, String key, Bandwidth bandwidth) {
        Cache<String, Bucket> cache = cachesByRule.computeIfAbsent(ruleName, r ->
                Caffeine.newBuilder()
                        .expireAfterAccess(Duration.ofMinutes(10))
                        .maximumSize(50_000) // hard ceiling per rule, just in case
                        .build()
        );
        return cache.get(key, k -> Bucket.builder().addLimit(bandwidth).build());
    }

    // ── Predefined limit tiers ────────────────────────────────────────────────
    public static Bandwidth strict() {
        // 5 requests per minute — login, per-order tracking guesses
        return Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
    }

    public static Bandwidth moderate() {
        // 10 requests per minute — public forms (inquiries, contact, demo-orders, catalogue)
        return Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
    }

    public static Bandwidth lenient() {
        // 30 requests per minute — newsletter, general IP backstop on tracking
        return Bandwidth.classic(30, Refill.greedy(30, Duration.ofMinutes(1)));
    }
    public static Bandwidth veryLenient() {
        // 60 requests per minute — post-payment saves, where the real cost of
        // a false positive (a paying customer's order looking "unreconciled")
        // outweighs the low abuse risk (money already changed hands).
        return Bandwidth.classic(60, Refill.greedy(60, Duration.ofMinutes(1)));
    }
}