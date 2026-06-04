package com.moving.reservation.admin;

import com.moving.reservation.reservation.Reservation;
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

    @Transactional
    public void record(Reservation reservation, String action, String detail, String createdBy) {
        adminAuditLogRepository.save(new AdminAuditLog(
                reservation,
                action,
                detail,
                createdBy == null || createdBy.isBlank() ? "system" : createdBy
        ));
    }
}
