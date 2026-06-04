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
    private final EmailNotificationSender emailNotificationSender;

    public CustomerNotificationService(CustomerNotificationRepository customerNotificationRepository,
                                       EmailNotificationSender emailNotificationSender) {
        this.customerNotificationRepository = customerNotificationRepository;
        this.emailNotificationSender = emailNotificationSender;
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

    public List<CustomerNotification> search(NotificationChannel channel, NotificationStatus status, String keyword) {
        return customerNotificationRepository.search(channel, status, normalizeKeyword(keyword));
    }

    @Transactional
    public EmailNotificationSendResult sendReadyEmails(Long reservationId) {
        return sendEmailsByStatus(reservationId, NotificationStatus.READY);
    }

    @Transactional
    public EmailNotificationSendResult resendFailedEmails(Long reservationId) {
        return sendEmailsByStatus(reservationId, NotificationStatus.FAILED);
    }

    private EmailNotificationSendResult sendEmailsByStatus(Long reservationId, NotificationStatus status) {
        List<CustomerNotification> notifications = customerNotificationRepository
                .findByReservationIdAndChannelAndStatusOrderByCreatedAtAsc(
                        reservationId,
                        NotificationChannel.EMAIL,
                        status
                );

        int sentCount = 0;
        int failedCount = 0;

        for (CustomerNotification notification : notifications) {
            try {
                emailNotificationSender.send(notification);
                notification.markSent();
                sentCount++;
            } catch (IllegalArgumentException | IllegalStateException exception) {
                notification.markFailed(exception.getMessage());
                failedCount++;
            } catch (RuntimeException exception) {
                notification.markFailed("이메일 발송 중 오류가 발생했습니다.");
                failedCount++;
            }
        }

        return new EmailNotificationSendResult(sentCount, failedCount);
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

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }

        String trimmedKeyword = keyword.trim();
        if (trimmedKeyword.isEmpty()) {
            return null;
        }

        return trimmedKeyword.toLowerCase();
    }
}
