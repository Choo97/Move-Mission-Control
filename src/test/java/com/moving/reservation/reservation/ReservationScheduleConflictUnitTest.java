package com.moving.reservation.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.moving.reservation.coupon.CouponService;
import com.moving.reservation.availability.AvailabilityService;
import com.moving.reservation.notification.CustomerNotificationService;
import com.moving.reservation.privacy.PrivacyHashService;
import com.moving.reservation.review.ReviewService;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ReservationScheduleConflictUnitTest {

    @Test
    void 동일한_날짜와_시간의_활성예약이_있으면_시도이력을_남기고_예약을_차단한다() {
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        ReservationConflictAttemptService conflictAttemptService = mock(ReservationConflictAttemptService.class);
        AvailabilityService availabilityService = mock(AvailabilityService.class);
        ReservationCreateRequest request = reservationRequest();
        org.mockito.Mockito.doThrow(new ReservationScheduleConflictException())
                .when(availabilityService).ensureAvailable(request.getMoveDate(), request.getMoveTime());

        ReservationService reservationService = new ReservationService(
                reservationRepository,
                mock(ReservationStatusHistoryRepository.class),
                mock(ReservationPhotoRepository.class),
                mock(ReservationCustomerActionHistoryRepository.class),
                mock(ReservationCustomerRequestRepository.class),
                mock(ReservationPhotoStorage.class),
                mock(CouponService.class),
                mock(ReviewService.class),
                mock(ReservationEstimateCalculator.class),
                conflictAttemptService,
                availabilityService,
                true,
                mock(CustomerNotificationService.class),
                5,
                10,
                mock(PrivacyHashService.class)
        );

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(ReservationScheduleConflictException.class)
                .hasMessage("선택한 날짜와 시간에는 이미 예약이 있습니다. 다른 시간을 선택해 주세요.");

        verify(conflictAttemptService).record(request);
        verify(reservationRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private ReservationCreateRequest reservationRequest() {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setCustomerName("중복시도고객");
        request.setPhone("010-2222-2222");
        request.setMoveDate(LocalDate.now().plusDays(30));
        request.setMoveTime(LocalTime.of(9, 0));
        request.setFromAddress("서울시 강남구 테헤란로 1");
        request.setToAddress("서울시 송파구 올림픽로 1");
        request.setMoveType(MoveType.STUDIO);
        request.setFromFloor(3);
        request.setToFloor(5);
        return request;
    }
}
