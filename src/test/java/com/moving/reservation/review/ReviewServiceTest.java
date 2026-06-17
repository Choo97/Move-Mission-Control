package com.moving.reservation.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.moving.reservation.reservation.MoveType;
import com.moving.reservation.reservation.Reservation;
import com.moving.reservation.reservation.ReservationCreateRequest;
import com.moving.reservation.reservation.ReservationService;
import com.moving.reservation.reservation.ReservationStatus;
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
class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    void 완료된_예약은_리뷰를_작성할_수_있다() {
        Reservation reservation = completedReservation("010-1234-5678");
        ReviewCreateRequest request = reviewCreateRequest(reservation.getId(), "010-1234-5678", 5, "정확하고 친절했습니다.");

        Review review = reviewService.create(request);

        assertThat(review.getId()).isNotNull();
        assertThat(review.getReservation().getId()).isEqualTo(reservation.getId());
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getContent()).isEqualTo("정확하고 친절했습니다.");
        assertThat(reviewRepository.existsByReservationId(reservation.getId())).isTrue();
    }

    @Test
    void 완료되지_않은_예약은_리뷰를_작성할_수_없다() {
        Reservation reservation = reservationService.create(reservationCreateRequest("010-1234-5678"));
        ReviewCreateRequest request = reviewCreateRequest(reservation.getId(), "010-1234-5678", 5, "아직 완료 전입니다.");

        assertThatThrownBy(() -> reviewService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("완료된 예약만 리뷰를 작성할 수 있습니다.");
    }

    @Test
    void 연락처가_다르면_리뷰를_작성할_수_없다() {
        Reservation reservation = completedReservation("010-1234-5678");
        ReviewCreateRequest request = reviewCreateRequest(reservation.getId(), "010-0000-0000", 5, "연락처가 다릅니다.");

        assertThatThrownBy(() -> reviewService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 번호와 연락처가 일치하지 않습니다.");
    }

    @Test
    void 같은_예약에는_리뷰를_한_번만_작성할_수_있다() {
        Reservation reservation = completedReservation("010-1234-5678");
        reviewService.create(reviewCreateRequest(reservation.getId(), "010-1234-5678", 5, "첫 번째 리뷰입니다."));

        assertThatThrownBy(() -> reviewService.create(
                reviewCreateRequest(reservation.getId(), "010-1234-5678", 4, "두 번째 리뷰입니다.")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 리뷰가 등록된 예약입니다.");
    }

    @Test
    void 리뷰_평균점수를_계산한다() {
        Reservation firstReservation = completedReservation("010-1111-1111");
        Reservation secondReservation = completedReservation("010-2222-2222");
        reviewService.create(reviewCreateRequest(firstReservation.getId(), "010-1111-1111", 5, "만족합니다."));
        reviewService.create(reviewCreateRequest(secondReservation.getId(), "010-2222-2222", 3, "보통입니다."));

        assertThat(reviewService.averageRating()).isEqualTo(4.0);
    }

    private Reservation completedReservation(String phone) {
        Reservation reservation = reservationService.create(reservationCreateRequest(phone));
        reservationService.updateStatus(reservation.getId(), ReservationStatus.CONSULTING, "admin");
        reservationService.updateStatus(reservation.getId(), ReservationStatus.ESTIMATE_SENT, "admin");
        reservationService.updateStatus(reservation.getId(), ReservationStatus.CONFIRMED, "admin");
        reservationService.updateStatus(reservation.getId(), ReservationStatus.COMPLETED, "admin");
        return reservation;
    }

    private ReservationCreateRequest reservationCreateRequest(String phone) {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setCustomerName("리뷰고객");
        request.setPhone(phone);
        request.setEmail("review@example.com");
        request.setMoveDate(LocalDate.now().plusDays(7));
        request.setMoveTime(LocalTime.of(10, 30));
        request.setFromAddress("서울시 강남구 테헤란로 1");
        request.setToAddress("서울시 송파구 올림픽로 1");
        request.setMoveType(MoveType.STUDIO);
        request.setFromElevator(true);
        request.setToElevator(true);
        request.setFromFloor(3);
        request.setToFloor(5);
        request.setMemo("리뷰 테스트 예약입니다.");
        return request;
    }

    private ReviewCreateRequest reviewCreateRequest(Long reservationId, String phone, Integer rating, String content) {
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setReservationId(reservationId);
        request.setPhone(phone);
        request.setRating(rating);
        request.setContent(content);
        return request;
    }
}
