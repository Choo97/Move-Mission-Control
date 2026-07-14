package com.moving.reservation.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.moving.reservation.coupon.CouponRepository;
import com.moving.reservation.reservation.ReservationRepository;
import com.moving.reservation.reservation.ServiceRequestType;
import com.moving.reservation.review.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "demo.seed.enabled=true",
        "spring.datasource.url=jdbc:h2:mem:demo-data-initializer-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"
})
@ActiveProfiles("test")
class DemoDataInitializerTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Test
    void seedsGeneralAndNonProfitDemoDataIntoAnEmptyDatabase() {
        assertThat(reservationRepository.count()).isEqualTo(12);
        assertThat(reservationRepository.findAll())
                .filteredOn(reservation -> reservation.getServiceType() == ServiceRequestType.GENERAL)
                .hasSize(6);
        assertThat(reservationRepository.findAll())
                .filteredOn(reservation -> reservation.getServiceType() == ServiceRequestType.NON_PROFIT)
                .hasSize(6)
                .allSatisfy(reservation -> {
                    assertThat(reservation.getEstimatedPrice()).isNull();
                    assertThat(reservation.getCouponCode()).isNull();
                });
        assertThat(reviewRepository.count()).isEqualTo(2);
        assertThat(couponRepository.findByCode("DEMO10")).isPresent();
    }
}
