package com.moving.reservation.admin;

import java.time.LocalDateTime;

public record AdminAuditLogResponse(
        Long id,
        String action,
        String detail,
        String createdBy,
        String ipAddress,
        String userAgent,
        LocalDateTime createdAt
) {

    public static AdminAuditLogResponse from(AdminAuditLog auditLog) {
        return new AdminAuditLogResponse(
                auditLog.getId(),
                auditLog.getAction(),
                auditLog.getDetail(),
                auditLog.getCreatedBy(),
                fallback(auditLog.getIpAddress()),
                fallback(auditLog.getUserAgent()),
                auditLog.getCreatedAt()
        );
    }

    private static String fallback(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }

        return value;
    }
}
