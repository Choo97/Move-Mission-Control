package com.moving.reservation.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.moving.reservation.notification.CustomerNotificationRepository;
import java.time.LocalDate;
import java.time.LocalTime;
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
