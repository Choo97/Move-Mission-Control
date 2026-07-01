package com.moving.reservation.notification;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerNotificationRepository extends JpaRepository<CustomerNotification, Long> {

    List<CustomerNotification> findByReservationIdOrderByCreatedAtDesc(Long reservationId);

    List<CustomerNotification> findByReservationIdAndChannelAndStatusOrderByCreatedAtAsc(
            Long reservationId,
            NotificationChannel channel,
            NotificationStatus status
    );

    long countByChannelAndStatus(NotificationChannel channel, NotificationStatus status);

    @Query("""
            select notification
            from CustomerNotification notification
            join fetch notification.reservation reservation
            where notification.status in :statuses
            order by notification.createdAt desc
            """)
    List<CustomerNotification> findByStatusInFetchReservation(@Param("statuses") Collection<NotificationStatus> statuses);

    @Query("""
            select notification
            from CustomerNotification notification
            join fetch notification.reservation reservation
            where (:channel is null or notification.channel = :channel)
              and (:status is null or notification.status = :status)
            order by notification.createdAt desc
            """)
    List<CustomerNotification> search(@Param("channel") NotificationChannel channel,
                                      @Param("status") NotificationStatus status);
}
