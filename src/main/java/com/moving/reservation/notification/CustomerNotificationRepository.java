package com.moving.reservation.notification;

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
            where (:channel is null or notification.channel = :channel)
              and (:status is null or notification.status = :status)
              and (:keyword is null
                or lower(reservation.customerName) like concat('%', :keyword, '%')
                or reservation.phone like concat('%', :keyword, '%')
                or lower(coalesce(reservation.email, '')) like concat('%', :keyword, '%')
                or lower(notification.recipientPhone) like concat('%', :keyword, '%'))
            order by notification.createdAt desc
            """)
    List<CustomerNotification> search(@Param("channel") NotificationChannel channel,
                                      @Param("status") NotificationStatus status,
                                      @Param("keyword") String keyword);
}
