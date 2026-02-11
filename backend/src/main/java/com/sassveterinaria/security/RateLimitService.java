package com.sassveterinaria.security;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class RateLimitService {

    private final SecurityHardeningProperties properties;
    private final Map<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    public RateLimitService(SecurityHardeningProperties properties) {
        this.properties = properties;
    }

    public void checkLogin(String usernameOrEmail) {
        String normalized = normalize(usernameOrEmail);
        String key = "login|" + currentIp() + "|" + (normalized == null ? "-" : normalized);
        enforce(
            key,
            properties.getRateLimit().getLoginLimit(),
            properties.getRateLimit().getLoginWindowSeconds()
        );
    }

    public void checkRefresh(String refreshToken) {
        String tokenPart = refreshToken == null ? "-" : Integer.toHexString(refreshToken.hashCode());
        String key = "refresh|" + currentIp() + "|" + tokenPart;
        enforce(
            key,
            properties.getRateLimit().getRefreshLimit(),
            properties.getRateLimit().getRefreshWindowSeconds()
        );
    }

    public void checkReports(AuthPrincipal principal) {
        String key = "report|" + principal.getUserId() + "|" + principal.getBranchId();
        enforce(
            key,
            properties.getRateLimit().getReportLimit(),
            properties.getRateLimit().getReportWindowSeconds()
        );
    }

    private void enforce(String key, int limit, int windowSeconds) {
        long now = Instant.now().getEpochSecond();
        long windowStart = now - windowSeconds;
        Deque<Long> deque = buckets.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (deque) {
            while (!deque.isEmpty() && deque.peekFirst() <= windowStart) {
                deque.pollFirst();
            }
            if (deque.size() >= limit) {
                long retryAfter = Math.max(1, (deque.peekFirst() + windowSeconds) - now);
                throw new RateLimitExceededException("Rate limit exceeded.", retryAfter);
            }
            deque.addLast(now);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase();
        return normalized.isEmpty() ? null : normalized;
    }

    private String currentIp() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return "unknown";
        }
        String forwarded = servletAttributes.getRequest().getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int commaIndex = forwarded.indexOf(',');
            return commaIndex >= 0 ? forwarded.substring(0, commaIndex).trim() : forwarded.trim();
        }
        String remoteAddr = servletAttributes.getRequest().getRemoteAddr();
        return remoteAddr == null || remoteAddr.isBlank() ? "unknown" : remoteAddr;
    }
}
