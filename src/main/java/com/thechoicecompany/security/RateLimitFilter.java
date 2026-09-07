package com.thechoicecompany.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Applies per-endpoint rate limits to all public (unauthenticated) POST/GET
 * routes before Spring Security's JWT filter runs. See SecurityConfig for
 * filter ordering.
 */
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private record Rule(
            String name,
            HttpMethod method,
            String pattern,
            Bandwidth bandwidth,
            Function<HttpServletRequest, String> extraKeyFn // null = IP-only key
    ) {}

    private final List<Rule> rules = List.of(
            new Rule("login",       HttpMethod.POST, "/api/auth/login",        RateLimiterService.strict(),   null),
            new Rule("inquiries",   HttpMethod.POST, "/api/inquiries",         RateLimiterService.moderate(), null),
            new Rule("contact",     HttpMethod.POST, "/api/contact",           RateLimiterService.moderate(), null),

            // CHANGED: was moderate() (10/min) — raised to veryLenient() (60/min).
            // This endpoint only fires post-payment; a false-positive block here
            // means a paying customer's order looks "unreconciled" for no reason.
            // Kept as a DoS backstop, not a real abuse control.
            new Rule("demo-orders", HttpMethod.POST, "/api/demo-orders",       RateLimiterService.veryLenient(), null),

            new Rule("newsletter",  HttpMethod.POST, "/api/newsletter/**",     RateLimiterService.lenient(),  null),
            new Rule("catalogue",   HttpMethod.POST, "/api/catalogue/request", RateLimiterService.moderate(), null),
            new Rule("setup-first-admin", HttpMethod.POST, "/api/setup/first-admin", RateLimiterService.strict(), null),

            // Order tracking — two layers of protection, both must pass:
            new Rule("order-track-by-order", HttpMethod.GET, "/api/orders/track",
                    RateLimiterService.strict(),
                    req -> req.getParameter("orderId")),
            new Rule("order-track-by-ip", HttpMethod.GET, "/api/orders/track",
                    RateLimiterService.lenient(),
                    null)
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        List<Rule> matched = rules.stream()
                .filter(r -> r.method().matches(request.getMethod())
                        && pathMatcher.match(r.pattern(), request.getRequestURI()))
                .toList();

        if (matched.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = resolveClientIp(request);

        for (Rule rule : matched) {
            String extra = rule.extraKeyFn() != null ? rule.extraKeyFn().apply(request) : null;
            String bucketKey = (extra != null && !extra.isBlank())
                    ? rule.name() + ":" + ip + ":" + extra
                    : rule.name() + ":" + ip;

            Bucket bucket = rateLimiterService.resolveBucket(rule.name(), bucketKey, rule.bandwidth());
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if (!probe.isConsumed()) {
                long waitSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000L;
                response.setStatus(429);
                response.setHeader("Retry-After", String.valueOf(waitSeconds));
                response.setContentType("application/json");
                response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                        "success", false,
                        "message", "Too many requests. Please try again in " + waitSeconds + "s.",
                        "error", "RATE_LIMITED"
                )));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}