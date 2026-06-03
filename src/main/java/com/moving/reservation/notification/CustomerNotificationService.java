package com.moving.reservation.notification;

import com.moving.reservation.reservation.Reservation;
import com.moving.reservation.reservation.ReservationStatus;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerNotificationService {

    private final CustomerNotificationRepository customerNotificationRepository;

    public CustomerNotificationService(CustomerNotificationRepository customerNotificationRepository) {
        this.customerNotificationRepository = customerNotificationRepository;
    }

    @Transactional
    public void prepareReservationCreated(Reservation reservation) {
        save(reservation, NotificationType.RESERVATION_CREATED,
                reservation.getCustomerName() + "님, 이사 예약이 접수되었습니다. 예약 번호는 "
                        + reservation.getId() + "번입니다.");

        if (reservation.hasEmail()) {
            saveEmail(reservation, NotificationType.RESERVATION_CREATED,
                    "[Move Mission Control] 예약 번호 " + reservation.getId()
                            + "번 접수 안내: " + reservation.getMoveDate() + " "
                            + reservation.getMoveTime() + " 이사 예약이 접수되었습니다.");
        }
    }

    @Transactional
    public void prepareStatusChanged(Reservation reservation, ReservationStatus status) {
        save(reservation, NotificationType.STATUS_CHANGED,
                reservation.getCustomerName() + "님, 예약 상태가 '" + status.getLabel() + "'(으)로 변경되었습니다.");
    }

    @Transactional
    public void prepareEstimateUpdated(Reservation reservation) {
        String estimateText = reservation.getFinalEstimatedPrice() == null
                ? "상담 후 안내"
                : NumberFormat.getNumberInstance(Locale.KOREA).format(reservation.getFinalEstimatedPrice()) + "원";
        save(reservation, NotificationType.ESTIMATE_UPDATED,
                reservation.getCustomerName() + "님, 이사 견적이 " + estimateText + "(으)로 안내 준비되었습니다.");
    }

    public List<CustomerNotification> findByReservationId(Long reservationId) {
        return customerNotificationRepository.findByReservationIdOrderByCreatedAtDesc(reservationId);
    }

    private void save(Reservation reservation, NotificationType type, String message) {
        customerNotificationRepository.save(new CustomerNotification(
                reservation,
                type,
                NotificationChannel.SMS,
                reservation.getPhone(),
                message
        ));
    }

    private void saveEmail(Reservation reservation, NotificationType type, String message) {
        customerNotificationRepository.save(new CustomerNotification(
                reservation,
                type,
                NotificationChannel.EMAIL,
                reservation.getEmail(),
                message
        ));
    }
}
