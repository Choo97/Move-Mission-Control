package com.moving.reservation.availability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
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
    private ReservationRepository reservationRepository;
    private AvailabilityService availabilityService;

    @BeforeEach
    void setUp() {
        scheduleRepository = mock(OperatingScheduleRepository.class);
        holidayRepository = mock(OperatingHolidayRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        availabilityService = new AvailabilityService(scheduleRepository, holidayRepository, reservationRepository);
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
}
