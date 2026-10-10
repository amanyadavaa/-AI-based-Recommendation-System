package com.guvi.recommend.web.security;

import com.sun.net.httpserver.Headers;

/**
 * Enterprise HTTP Security Headers Filter providing defense against XSS, clickjacking,
 * MIME-type sniffing, and insecure iframe embeds.
 */
public class SecurityHeadersFilter {

    public static final String CSP_POLICY =
            "default-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net https://unpkg.com https://cdn.tailwindcss.com https://cdnjs.cloudflare.com https://images.unsplash.com https://tile.openstreetmap.org data: blob:; " +
            "img-src 'self' https: data: blob:; " +
            "connect-src 'self' http: https:; " +
            "font-src 'self' https://cdnjs.cloudflare.com https://fonts.gstatic.com data:; " +
            "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://unpkg.com https://cdn.tailwindcss.com https://cdnjs.cloudflare.com https://fonts.googleapis.com;";

    public static void applyHeaders(Headers headers) {
        if (headers == null) return;
        headers.set("Content-Security-Policy", CSP_POLICY);
        headers.set("X-Content-Type-Options", "nosniff");
        headers.set("X-Frame-Options", "SAMEORIGIN");
        headers.set("X-XSS-Protection", "1; mode=block");
        headers.set("Referrer-Policy", "strict-origin-when-cross-origin");
        headers.set("Permissions-Policy", "geolocation=(self), microphone=(), camera=()");
        headers.set("X-Permitted-Cross-Domain-Policies", "none");
    }
}
