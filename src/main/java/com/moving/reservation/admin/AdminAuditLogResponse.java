package com.moving.reservation.admin;

import java.time.LocalDateTime;

public record AdminAuditLogResponse(
        Long id,
        String action,
        String detail,
        String createdBy,
        LocalDateTime createdAt
) {

    public static AdminAuditLogResponse from(AdminAuditLog auditLog) {
        return new AdminAuditLogResponse(
                auditLog.getId(),
                auditLog.getAction(),
                auditLog.getDetail(),
                auditLog.getCreatedBy(),
                auditLog.getCreatedAt()
        );
    }
}
