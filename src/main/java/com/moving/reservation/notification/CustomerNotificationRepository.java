package com.moving.reservation.notification;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerNotificationRepository extends JpaRepository<CustomerNotification, Long> {

    List<CustomerNotification> findByReservationIdOrderByCreatedAtDesc(Long reservationId);

    List<CustomerNotification> findByReservationIdAndChannelAndStatusOrderByCreatedAtAsc(
            Long reservationId,
            NotificationChannel channel,
            NotificationStatus status
    );
}
