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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Comma-separated trusted proxy CIDRs/IPs injected from config.
    // Example: app.trusted-proxies=127.0.0.1,10.0.0.0/8,172.16.0.0/12
    // Set to the IP(s) of your Nginx/load-balancer only.
    @Value("${app.trusted-proxies:127.0.0.1}")
    private List<String> trustedProxies;

    private record Rule(
            String name,
            HttpMethod method,
            String pattern,
            Bandwidth bandwidth,
            Function<HttpServletRequest, String> extraKeyFn
    ) {}

    private final List<Rule> rules = List.of(
            new Rule("login",       HttpMethod.POST, "/api/auth/login",        RateLimiterService.strict(),      null),
            new Rule("inquiries",   HttpMethod.POST, "/api/inquiries",         RateLimiterService.moderate(),    null),
            new Rule("contact",     HttpMethod.POST, "/api/contact",           RateLimiterService.moderate(),    null),
            new Rule("demo-orders", HttpMethod.POST, "/api/demo-orders",       RateLimiterService.veryLenient(), null),
            new Rule("newsletter",  HttpMethod.POST, "/api/newsletter/**",     RateLimiterService.lenient(),     null),
            new Rule("catalogue",   HttpMethod.POST, "/api/catalogue/request", RateLimiterService.moderate(),    null),
            new Rule("setup-first-admin", HttpMethod.POST, "/api/setup/first-admin", RateLimiterService.strict(), null),
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

    /**
     * Returns the real client IP, trusting X-Forwarded-For ONLY when the
     * direct TCP peer (getRemoteAddr) is a known trusted proxy.
     *
     * Without this check, any client can spoof X-Forwarded-For and bypass
     * per-IP rate limits entirely.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();

        if (isTrustedProxy(remoteAddr)) {
            // We're behind a proxy we trust — use the first IP in X-Forwarded-For
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                // X-Forwarded-For can be a comma-separated chain: "client, proxy1, proxy2"
                // Take the first (leftmost) value — that's the original client
                return forwarded.split(",")[0].trim();
            }
        }

        // Direct connection or untrusted proxy: use the real TCP peer address
        return remoteAddr;
    }

    private boolean isTrustedProxy(String remoteAddr) {
        if (remoteAddr == null) return false;
        return trustedProxies.stream().anyMatch(trusted -> {
            // Simple exact-match for IPs. For CIDR support, add a library
            // like Apache Commons Net or Spring Security's IpAddressMatcher.
            return trusted.trim().equals(remoteAddr);
        });
    }
}
