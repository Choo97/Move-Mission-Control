package com.moving.reservation.admin;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdminAuditLogRepository extends JpaRepository<AdminAuditLog, Long> {

    List<AdminAuditLog> findByReservationIdOrderByCreatedAtDesc(Long reservationId);

    @Query("""
            select auditLog
            from AdminAuditLog auditLog
            left join fetch auditLog.reservation reservation
            where (:action is null or auditLog.action = :action)
              and (:createdBy is null or lower(auditLog.createdBy) like concat('%', :createdBy, '%'))
              and (:keyword is null
                    or lower(auditLog.action) like concat('%', :keyword, '%')
                    or lower(auditLog.detail) like concat('%', :keyword, '%')
                    or lower(reservation.customerName) like concat('%', :keyword, '%')
                    or reservation.phone like concat('%', :keyword, '%'))
              and (:startAt is null or auditLog.createdAt >= :startAt)
              and (:endAt is null or auditLog.createdAt < :endAt)
            order by auditLog.createdAt desc
            """)
    List<AdminAuditLog> search(String action, String createdBy, String keyword,
                               LocalDateTime startAt, LocalDateTime endAt);

    @Query("select distinct auditLog.action from AdminAuditLog auditLog order by auditLog.action asc")
    List<String> findDistinctActions();
}
