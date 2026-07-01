package com.moving.reservation.reservation;

import jakarta.servlet.http.HttpServletRequest;

record ReservationLookupClientInfo(String ipAddress) {

    private static final String UNKNOWN = "unknown";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Real-IP";

    static ReservationLookupClientInfo from(HttpServletRequest request) {
        if (request == null) {
            return new ReservationLookupClientInfo(UNKNOWN);
        }

        return new ReservationLookupClientInfo(clientIp(request));
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

        String remoteAddress = request.getRemoteAddr();

        if (hasText(remoteAddress)) {
            return remoteAddress.trim();
        }

        return UNKNOWN;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
