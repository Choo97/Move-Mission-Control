package com.moving.reservation.availability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.moving.reservation.reservation.Reservation;
import com.moving.reservation.reservation.ReservationRepository;
import com.moving.reservation.reservation.ReservationScheduleConflictException;
import com.moving.reservation.reservation.ReservationStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AvailabilityServiceTest {

    private OperatingScheduleRepository scheduleRepository;
    private OperatingHolidayRepository holidayRepository;
    private OperatingPolicyRepository policyRepository;
    private ReservationRepository reservationRepository;
    private AvailabilityService availabilityService;

    @BeforeEach
    void setUp() {
        scheduleRepository = mock(OperatingScheduleRepository.class);
        holidayRepository = mock(OperatingHolidayRepository.class);
        policyRepository = mock(OperatingPolicyRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        when(policyRepository.findById(OperatingPolicy.DEFAULT_ID)).thenReturn(Optional.of(OperatingPolicy.defaults()));
        availabilityService = new AvailabilityService(
                scheduleRepository,
                holidayRepository,
                policyRepository,
                reservationRepository
        );
    }

    @Test
    void 지정_휴무일에는_예약가능한_시간이_없다() {
        LocalDate date = LocalDate.now().plusDays(10);
        when(holidayRepository.findByHolidayDate(date))
                .thenReturn(Optional.of(new OperatingHoliday(date, "여름 휴무")));

        AvailabilityResponse response = availabilityService.availability(date);

        assertThat(response.closed()).isTrue();
        assertThat(response.closureReason()).isEqualTo("여름 휴무");
        assertThat(response.availableTimes()).isEmpty();
    }

    @Test
    void 운영시간을_간격별로_나누고_이미_예약된_시간은_제외한다() {
        LocalDate date = LocalDate.now().plusDays(11);
        OperatingSchedule schedule = new OperatingSchedule(
                date.getDayOfWeek(), true, LocalTime.of(9, 0), LocalTime.of(12, 0), 60);
        Reservation reserved = mock(Reservation.class);
        when(reserved.getMoveTime()).thenReturn(LocalTime.of(10, 0));
        when(reserved.getId()).thenReturn(1L);
        when(holidayRepository.findByHolidayDate(date)).thenReturn(Optional.empty());
        when(scheduleRepository.findByDayOfWeek(date.getDayOfWeek())).thenReturn(Optional.of(schedule));
        when(reservationRepository.findByMoveDateAndStatusNot(date, ReservationStatus.CANCELED))
                .thenReturn(List.of(reserved));

        assertThat(availabilityService.availability(date).availableTimes())
                .containsExactly(LocalTime.of(9, 0), LocalTime.of(11, 0));
    }

    @Test
    void 휴무일_예약을_검증하면_고객용_안내예외가_발생한다() {
        LocalDate date = LocalDate.now().plusDays(12);
        when(holidayRepository.findByHolidayDate(date))
                .thenReturn(Optional.of(new OperatingHoliday(date, "정기 점검")));

        assertThatThrownBy(() -> availabilityService.ensureAvailable(date, LocalTime.of(9, 0)))
                .isInstanceOf(ReservationScheduleConflictException.class)
                .hasMessage("정기 점검입니다. 다른 날짜를 선택해 주세요.");
    }

    @Test
    void 예약가능_기간보다_이른_날짜는_마감으로_응답한다() {
        LocalDate date = LocalDate.now().plusDays(1);
        when(policyRepository.findById(OperatingPolicy.DEFAULT_ID))
                .thenReturn(Optional.of(new OperatingPolicyTestFactory().policy(2, 30, 8)));

        AvailabilityResponse response = availabilityService.availability(date);

        assertThat(response.closed()).isTrue();
        assertThat(response.closureReason()).isEqualTo("예약 가능 시작일 전");
        assertThat(response.availableTimes()).isEmpty();
    }

    @Test
    void 예약가능_기간보다_먼_날짜는_마감으로_응답한다() {
        LocalDate date = LocalDate.now().plusDays(31);
        when(policyRepository.findById(OperatingPolicy.DEFAULT_ID))
                .thenReturn(Optional.of(new OperatingPolicyTestFactory().policy(0, 30, 8)));

        AvailabilityResponse response = availabilityService.availability(date);

        assertThat(response.closed()).isTrue();
        assertThat(response.closureReason()).isEqualTo("예약 가능 기간 초과");
        assertThat(response.availableTimes()).isEmpty();
    }

    @Test
    void 하루_최대예약건수에_도달하면_예약가능한_시간이_없다() {
        LocalDate date = LocalDate.now().plusDays(13);
        OperatingSchedule schedule = new OperatingSchedule(
                date.getDayOfWeek(), true, LocalTime.of(9, 0), LocalTime.of(12, 0), 60);
        Reservation firstReservation = reservation(1L, LocalTime.of(9, 0));
        Reservation secondReservation = reservation(2L, LocalTime.of(10, 0));

        when(policyRepository.findById(OperatingPolicy.DEFAULT_ID))
                .thenReturn(Optional.of(new OperatingPolicyTestFactory().policy(0, 30, 2)));
        when(holidayRepository.findByHolidayDate(date)).thenReturn(Optional.empty());
        when(scheduleRepository.findByDayOfWeek(date.getDayOfWeek())).thenReturn(Optional.of(schedule));
        when(reservationRepository.findByMoveDateAndStatusNot(date, ReservationStatus.CANCELED))
                .thenReturn(List.of(firstReservation, secondReservation));

        AvailabilityResponse response = availabilityService.availability(date);

        assertThat(response.closed()).isTrue();
        assertThat(response.closureReason()).isEqualTo("하루 최대 예약 건수 도달");
        assertThat(response.availableTimes()).isEmpty();
    }

    @Test
    void 운영정책을_저장한다() {
        OperatingPolicy policy = OperatingPolicy.defaults();
        when(policyRepository.findById(OperatingPolicy.DEFAULT_ID)).thenReturn(Optional.of(policy));

        OperatingPolicy updated = availabilityService.updatePolicy(new OperatingPolicyRequest(1, 60, 6));

        assertThat(updated.getMinAdvanceDays()).isEqualTo(1);
        assertThat(updated.getMaxAdvanceDays()).isEqualTo(60);
        assertThat(updated.getMaxDailyReservations()).isEqualTo(6);
    }

    @Test
    void 운영정책은_예약가능_시작일이_종료일보다_크면_저장할수없다() {
        assertThatThrownBy(() -> availabilityService.updatePolicy(new OperatingPolicyRequest(10, 5, 6)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 가능 시작일은 종료일보다 작거나 같아야 합니다.");
    }

    @Test
    void 기본운영정책을_초기화한다() {
        when(policyRepository.findById(OperatingPolicy.DEFAULT_ID)).thenReturn(Optional.empty());
        when(scheduleRepository.findByDayOfWeek(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Optional.of(new OperatingSchedule(
                        java.time.DayOfWeek.MONDAY,
                        true,
                        LocalTime.of(9, 0),
                        LocalTime.of(18, 0),
                        60
                )));

        availabilityService.initializeDefaults();

        verify(policyRepository).save(org.mockito.ArgumentMatchers.any(OperatingPolicy.class));
    }

    private Reservation reservation(Long id, LocalTime moveTime) {
        Reservation reservation = mock(Reservation.class);
        when(reservation.getId()).thenReturn(id);
        when(reservation.getMoveTime()).thenReturn(moveTime);
        return reservation;
    }

    private static class OperatingPolicyTestFactory {

        private OperatingPolicy policy(int minAdvanceDays, int maxAdvanceDays, int maxDailyReservations) {
            OperatingPolicy policy = OperatingPolicy.defaults();
            policy.update(minAdvanceDays, maxAdvanceDays, maxDailyReservations);
            return policy;
        }
    }
}
