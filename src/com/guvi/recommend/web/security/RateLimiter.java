package com.guvi.recommend.web.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe Token Bucket Rate Limiter for HTTP DDoS and scraping abuse defense.
 * Default: 120 requests per minute with burst capacity of 120.
 */
public class RateLimiter {
    private final int capacity;
    private final double refillRatePerMillisecond;
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public RateLimiter(int requestsPerMinute) {
        this.capacity = requestsPerMinute;
        this.refillRatePerMillisecond = (double) requestsPerMinute / 60000.0;
    }

    public synchronized boolean tryAcquire(String clientId) {
        if (clientId == null || clientId.isEmpty()) clientId = "anonymous";
        TokenBucket bucket = buckets.computeIfAbsent(clientId, k -> new TokenBucket(capacity, refillRatePerMillisecond));
        return bucket.tryConsume(1);
    }

    public void reset() {
        buckets.clear();
    }

    public int getClientCount() {
        return buckets.size();
    }

    private static class TokenBucket {
        private final int capacity;
        private final double refillRate;
        private double tokens;
        private long lastRefillTimestamp;

        TokenBucket(int capacity, double refillRate) {
            this.capacity = capacity;
            this.refillRate = refillRate;
            this.tokens = capacity;
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        synchronized boolean tryConsume(int count) {
            refill();
            if (tokens >= count) {
                tokens -= count;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTimestamp;
            if (elapsed > 0) {
                tokens = Math.min(capacity, tokens + (elapsed * refillRate));
                lastRefillTimestamp = now;
            }
        }
    }
}
