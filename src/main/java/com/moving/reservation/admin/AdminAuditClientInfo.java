package com.moving.reservation.admin;

import jakarta.servlet.http.HttpServletRequest;

record AdminAuditClientInfo(String ipAddress, String userAgent) {

    private static final String UNKNOWN = "unknown";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Real-IP";
    private static final String USER_AGENT = "User-Agent";

    static AdminAuditClientInfo from(HttpServletRequest request) {
        if (request == null) {
            return new AdminAuditClientInfo(UNKNOWN, UNKNOWN);
        }

        return new AdminAuditClientInfo(clientIp(request), headerValue(request.getHeader(USER_AGENT)));
    }

    private static String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader(X_FORWARDED_FOR);

        if (hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader(X_REAL_IP);

        if (hasText(realIp)) {
            return realIp.trim();
        }

        return headerValue(request.getRemoteAddr());
    }

    private static String headerValue(String value) {
        if (!hasText(value)) {
            return UNKNOWN;
        }

        return value.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
