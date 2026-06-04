package com.moving.reservation.admin;

import com.moving.reservation.reservation.Reservation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminAuditLogService {

    private final AdminAuditLogRepository adminAuditLogRepository;

    public AdminAuditLogService(AdminAuditLogRepository adminAuditLogRepository) {
        this.adminAuditLogRepository = adminAuditLogRepository;
    }

    public List<AdminAuditLog> findByReservationId(Long reservationId) {
        return adminAuditLogRepository.findByReservationIdOrderByCreatedAtDesc(reservationId);
    }

    public List<AdminAuditLog> search(String action, String createdBy, String keyword,
                                      LocalDate startDate, LocalDate endDate) {
        return adminAuditLogRepository.search(
                normalizeExact(action),
                normalizeLike(createdBy),
                normalizeLike(keyword),
                startDate == null ? null : startDate.atStartOfDay(),
                endDate == null ? null : endDate.plusDays(1).atStartOfDay()
        );
    }

    public List<String> findActions() {
        return adminAuditLogRepository.findDistinctActions();
    }

    @Transactional
    public void record(Reservation reservation, String action, String detail, String createdBy) {
        adminAuditLogRepository.save(new AdminAuditLog(
                reservation,
                action,
                detail,
                createdBy == null || createdBy.isBlank() ? "system" : createdBy
        ));
    }

    private String normalizeExact(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private String normalizeLike(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toLowerCase();
    }
}
