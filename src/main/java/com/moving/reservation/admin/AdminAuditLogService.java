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

    private static final String SYSTEM_ACTOR = "system";
    private static final String UNKNOWN_CLIENT_VALUE = "unknown";
    private static final String RESERVATION_DETAIL_VIEW_ACTION = "예약 상세 조회";
    private static final int MAX_ACTOR_LENGTH = 50;
    private static final int MAX_IP_ADDRESS_LENGTH = 45;
    private static final int MAX_USER_AGENT_LENGTH = 255;

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
        record(reservation, action, detail, createdBy, null, null);
    }

    @Transactional
    public void record(Reservation reservation, String action, String detail, String createdBy,
                       String ipAddress, String userAgent) {
        adminAuditLogRepository.save(new AdminAuditLog(
                reservation,
                action,
                detail,
                normalizeActor(createdBy),
                normalizeClientValue(ipAddress, MAX_IP_ADDRESS_LENGTH),
                normalizeClientValue(userAgent, MAX_USER_AGENT_LENGTH)
        ));
    }

    @Transactional
    public void recordReservationDetailView(Reservation reservation, String createdBy,
                                            String ipAddress, String userAgent) {
        record(
                reservation,
                RESERVATION_DETAIL_VIEW_ACTION,
                "개인정보가 포함된 예약 상세를 조회했습니다.",
                createdBy,
                ipAddress,
                userAgent
        );
    }

    private String normalizeActor(String value) {
        if (value == null || value.isBlank()) {
            return SYSTEM_ACTOR;
        }

        return limit(value.trim(), MAX_ACTOR_LENGTH);
    }

    private String normalizeClientValue(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return UNKNOWN_CLIENT_VALUE;
        }

        return limit(value.trim(), maxLength);
    }

    private String limit(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength);
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
