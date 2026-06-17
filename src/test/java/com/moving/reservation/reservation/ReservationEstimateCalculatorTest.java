package com.moving.reservation.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationEstimateCalculatorTest {

    @Autowired
    private ReservationEstimateCalculator estimateCalculator;

    @Test
    void 기본포함거리_이내이면_거리추가요금이_붙지_않는다() {
        int estimatedPrice = estimateCalculator.calculate(
                MoveType.STUDIO,
                true,
                true,
                3,
                5,
                false,
                false,
                7
        );

        assertThat(estimatedPrice).isEqualTo(180000);
        assertThat(estimateCalculator.calculateLines(
                MoveType.STUDIO,
                true,
                true,
                3,
                5,
                false,
                false,
                7
        )).extracting(ReservationEstimateLine::amount)
                .containsExactly(180000, 0);
    }

    @Test
    void 기본포함거리를_초과한_거리만_km당_추가요금으로_계산한다() {
        int estimatedPrice = estimateCalculator.calculate(
                MoveType.STUDIO,
                true,
                true,
                1,
                1,
                false,
                false,
                15
        );

        assertThat(estimatedPrice).isEqualTo(230000);
    }

    @Test
    void 엘리베이터가_없고_사다리차가_없으면_고층작업_추가요금을_계산한다() {
        int estimatedPrice = estimateCalculator.calculate(
                MoveType.STUDIO,
                false,
                true,
                5,
                1,
                false,
                false,
                7
        );

        assertThat(estimatedPrice).isEqualTo(290000);
        assertThat(estimateCalculator.calculateLines(
                MoveType.STUDIO,
                false,
                true,
                5,
                1,
                false,
                false,
                7
        )).extracting(ReservationEstimateLine::amount)
                .containsExactly(180000, 50000, 60000, 0);
    }

    @Test
    void 사다리차를_선택하면_고층작업_추가요금은_중복으로_붙지_않는다() {
        int estimatedPrice = estimateCalculator.calculate(
                MoveType.STUDIO,
                false,
                true,
                5,
                1,
                true,
                false,
                7
        );

        assertThat(estimatedPrice).isEqualTo(350000);
        assertThat(estimateCalculator.calculateLines(
                MoveType.STUDIO,
                false,
                true,
                5,
                1,
                true,
                false,
                7
        )).extracting(ReservationEstimateLine::amount)
                .containsExactly(180000, 120000, 50000, 0);
    }
}
