package com.moving.reservation.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.moving.reservation.coupon.Coupon;
import com.moving.reservation.coupon.CouponRepository;
import com.moving.reservation.coupon.DiscountType;
import com.moving.reservation.notification.CustomerNotification;
import com.moving.reservation.notification.CustomerNotificationRepository;
import com.moving.reservation.notification.NotificationChannel;
import com.moving.reservation.notification.NotificationStatus;
import com.moving.reservation.notification.NotificationType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private CustomerNotificationRepository customerNotificationRepository;

    @Autowired
    private ReservationStatusHistoryRepository statusHistoryRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Test
    void 예약을_신청하면_예약번호와_연락처로_조회할_수_있다() {
        Reservation createdReservation = reservationService.create(reservationCreateRequest());

        ReservationSearchRequest searchRequest = new ReservationSearchRequest();
        searchRequest.setReservationId(createdReservation.getId());
        searchRequest.setPhone("010-1234-5678");

        Reservation foundReservation = reservationService.search(searchRequest);

        assertThat(foundReservation.getId()).isEqualTo(createdReservation.getId());
        assertThat(foundReservation.getCustomerName()).isEqualTo("홍길동");
        assertThat(foundReservation.getStatus()).isEqualTo(ReservationStatus.RECEIVED);
        assertThat(foundReservation.getEstimatedPrice()).isNotNull();
        assertThat(reservationRepository.count()).isEqualTo(1);
        assertThat(customerNotificationRepository.findByReservationIdOrderByCreatedAtDesc(createdReservation.getId()))
                .hasSize(2);
    }

    @Test
    void 연락처가_다르면_예약을_조회할_수_없다() {
        Reservation createdReservation = reservationService.create(reservationCreateRequest());

        ReservationSearchRequest searchRequest = new ReservationSearchRequest();
        searchRequest.setReservationId(createdReservation.getId());
        searchRequest.setPhone("010-0000-0000");

        assertThatThrownBy(() -> reservationService.search(searchRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 번호와 연락처가 일치하는 예약을 찾을 수 없습니다.");
    }

    @Test
    void 관리자가_예약상태를_변경하면_상태와_이력이_저장된다() {
        Reservation createdReservation = reservationService.create(reservationCreateRequest());

        reservationService.updateStatus(createdReservation.getId(), ReservationStatus.CONSULTING, "admin");

        Reservation updatedReservation = reservationService.get(createdReservation.getId());
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.CONSULTING);
        assertThat(statusHistoryRepository.findByReservationIdOrderByChangedAtDesc(createdReservation.getId()))
                .singleElement()
                .satisfies(history -> {
                    assertThat(history.getPreviousStatus()).isEqualTo(ReservationStatus.RECEIVED);
                    assertThat(history.getChangedStatus()).isEqualTo(ReservationStatus.CONSULTING);
                    assertThat(history.getChangedBy()).isEqualTo("admin");
                });
    }

    @Test
    void 완료된_예약은_상담중으로_되돌릴_수_없다() {
        Reservation createdReservation = reservationService.create(reservationCreateRequest());
        reservationService.updateStatus(createdReservation.getId(), ReservationStatus.CONSULTING, "admin");
        reservationService.updateStatus(createdReservation.getId(), ReservationStatus.ESTIMATE_SENT, "admin");
        reservationService.updateStatus(createdReservation.getId(), ReservationStatus.CONFIRMED, "admin");
        reservationService.updateStatus(createdReservation.getId(), ReservationStatus.COMPLETED, "admin");

        assertThatThrownBy(() -> reservationService.updateStatus(
                createdReservation.getId(),
                ReservationStatus.CONSULTING,
                "admin"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 상태에서는 '상담중'(으)로 변경할 수 없습니다.");

        assertThat(reservationService.get(createdReservation.getId()).getStatus()).isEqualTo(ReservationStatus.COMPLETED);
        assertThat(statusHistoryRepository.findByReservationIdOrderByChangedAtDesc(createdReservation.getId()))
                .hasSize(4);
    }

    @Test
    void 이메일이_있는_예약을_신청하면_이메일_발송준비_이력이_생성된다() {
        Reservation createdReservation = reservationService.create(reservationCreateRequest());

        List<CustomerNotification> notifications = customerNotificationRepository
                .findByReservationIdOrderByCreatedAtDesc(createdReservation.getId());

        assertThat(notifications)
                .filteredOn(notification -> notification.getChannel() == NotificationChannel.EMAIL)
                .singleElement()
                .satisfies(notification -> {
                    assertThat(notification.getType()).isEqualTo(NotificationType.RESERVATION_CREATED);
                    assertThat(notification.getStatus()).isEqualTo(NotificationStatus.READY);
                    assertThat(notification.getRecipientContact()).isEqualTo("customer@example.com");
                    assertThat(notification.getMessage()).contains("예약 번호 " + createdReservation.getId() + "번");
                    assertThat(notification.getSentAt()).isNull();
                    assertThat(notification.getFailureReason()).isNull();
                });
    }

    @Test
    void 이메일이_없는_예약을_신청하면_이메일_이력은_생성하지_않는다() {
        ReservationCreateRequest request = reservationCreateRequest();
        request.setEmail(null);

        Reservation createdReservation = reservationService.create(request);

        assertThat(customerNotificationRepository.findByReservationIdOrderByCreatedAtDesc(createdReservation.getId()))
                .filteredOn(notification -> notification.getChannel() == NotificationChannel.EMAIL)
                .isEmpty();
    }

    @Test
    void 정액쿠폰을_사용하면_최종견적에서_정해진_금액을_할인한다() {
        couponRepository.save(new Coupon("FIXED10000", "테스트 정액 쿠폰", DiscountType.FIXED, 10000));
        ReservationCreateRequest request = reservationCreateRequest();
        request.setCouponCode("fixed10000");

        Reservation createdReservation = reservationService.create(request);

        assertThat(createdReservation.getEstimatedPrice()).isEqualTo(180000);
        assertThat(createdReservation.getCouponCode()).isEqualTo("FIXED10000");
        assertThat(createdReservation.getCouponName()).isEqualTo("테스트 정액 쿠폰");
        assertThat(createdReservation.getDiscountAmount()).isEqualTo(10000);
        assertThat(createdReservation.getFinalEstimatedPrice()).isEqualTo(170000);
    }

    @Test
    void 정률쿠폰을_사용하면_최종견적에서_비율만큼_할인한다() {
        couponRepository.save(new Coupon("PERCENT10", "테스트 정률 쿠폰", DiscountType.PERCENT, 10));
        ReservationCreateRequest request = reservationCreateRequest();
        request.setCouponCode("PERCENT10");

        Reservation createdReservation = reservationService.create(request);

        assertThat(createdReservation.getEstimatedPrice()).isEqualTo(180000);
        assertThat(createdReservation.getDiscountAmount()).isEqualTo(18000);
        assertThat(createdReservation.getFinalEstimatedPrice()).isEqualTo(162000);
    }

    @Test
    void 비활성쿠폰은_예약에_사용할_수_없다() {
        Coupon coupon = new Coupon("DISABLED", "비활성 쿠폰", DiscountType.FIXED, 10000);
        coupon.deactivate();
        couponRepository.save(coupon);
        ReservationCreateRequest request = reservationCreateRequest();
        request.setCouponCode("DISABLED");

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용 가능한 쿠폰을 찾을 수 없습니다.");
    }

    private ReservationCreateRequest reservationCreateRequest() {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setCustomerName("홍길동");
        request.setPhone("010-1234-5678");
        request.setEmail("customer@example.com");
        request.setMoveDate(LocalDate.now().plusDays(7));
        request.setMoveTime(LocalTime.of(10, 30));
        request.setFromAddress("서울시 강남구 테헤란로 1");
        request.setToAddress("서울시 송파구 올림픽로 1");
        request.setMoveType(MoveType.STUDIO);
        request.setFromElevator(true);
        request.setToElevator(true);
        request.setFromFloor(3);
        request.setToFloor(5);
        request.setMemo("테스트 예약입니다.");
        return request;
    }
}
