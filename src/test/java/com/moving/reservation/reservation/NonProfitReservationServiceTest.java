package com.moving.reservation.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "reservation.schedule-conflict.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:non-profit-reservation-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"
})
@ActiveProfiles("test")
@Transactional
class NonProfitReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Test
    void nonProfitRequestIsStoredWithoutEstimateOrCoupon() {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setCustomerName("도움요청테스트");
        request.setPhone("010-0000-2999");
        request.setEmail("non-profit-test@example.com");
        request.setMoveDate(LocalDate.now().plusDays(7));
        request.setMoveTime(LocalTime.of(10, 0));
        request.setFromAddress("가상 출발지");
        request.setToAddress("가상 도착지");
        request.setMoveType(MoveType.STUDIO);
        request.setServiceType(ServiceRequestType.NON_PROFIT);
        request.setFromElevator(true);
        request.setToElevator(true);
        request.setFromFloor(2);
        request.setToFloor(3);
        request.setMemo("비영리 도움 요청 통합 테스트");
        request.setCouponCode("DEMO10");

        Reservation reservation = reservationService.create(request);

        assertThat(reservation.getServiceType()).isEqualTo(ServiceRequestType.NON_PROFIT);
        assertThat(reservation.getBaseEstimatedPrice()).isNull();
        assertThat(reservation.getEstimatedPrice()).isNull();
        assertThat(reservation.getCouponCode()).isNull();
        assertThat(reservationService.estimateLines(reservation)).isEmpty();
    }
}
