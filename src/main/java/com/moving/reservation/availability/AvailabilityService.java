package com.moving.reservation.availability;

import com.moving.reservation.reservation.Reservation;
import com.moving.reservation.reservation.ReservationRepository;
import com.moving.reservation.reservation.ReservationScheduleConflictException;
import com.moving.reservation.reservation.ReservationStatus;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true, noRollbackFor = ReservationScheduleConflictException.class)
public class AvailabilityService {

    private final OperatingScheduleRepository scheduleRepository;
    private final OperatingHolidayRepository holidayRepository;
    private final OperatingPolicyRepository policyRepository;
    private final ReservationRepository reservationRepository;

    public AvailabilityService(OperatingScheduleRepository scheduleRepository,
                               OperatingHolidayRepository holidayRepository,
                               OperatingPolicyRepository policyRepository,
                               ReservationRepository reservationRepository) {
        this.scheduleRepository = scheduleRepository;
        this.holidayRepository = holidayRepository;
        this.policyRepository = policyRepository;
        this.reservationRepository = reservationRepository;
    }

    public AvailabilityResponse availability(LocalDate date) {
        return availability(date, null);
    }

    private AvailabilityResponse availability(LocalDate date, Long excludedReservationId) {
        OperatingPolicy policy = policy();
        LocalDate earliestDate = LocalDate.now().plusDays(policy.getMinAdvanceDays());
        if (date.isBefore(earliestDate)) {
            return new AvailabilityResponse(date, true, "예약 가능 시작일 전", List.of());
        }

        LocalDate latestDate = LocalDate.now().plusDays(policy.getMaxAdvanceDays());
        if (date.isAfter(latestDate)) {
            return new AvailabilityResponse(date, true, "예약 가능 기간 초과", List.of());
        }

        OperatingHoliday holiday = holidayRepository.findByHolidayDate(date).orElse(null);
        if (holiday != null) {
            return new AvailabilityResponse(date, true, holiday.getReason(), List.of());
        }

        OperatingSchedule schedule = scheduleRepository.findByDayOfWeek(date.getDayOfWeek()).orElse(null);
        if (schedule == null || !schedule.isOpen()) {
            return new AvailabilityResponse(date, true, "정기 휴무일", List.of());
        }

        List<Reservation> activeReservations = reservationRepository
                .findByMoveDateAndStatusNot(date, ReservationStatus.CANCELED).stream()
                .filter(reservation -> !reservation.getId().equals(excludedReservationId))
                .toList();
        if (activeReservations.size() >= policy.getMaxDailyReservations()) {
            return new AvailabilityResponse(date, true, "하루 최대 예약 건수 도달", List.of());
        }

        Set<LocalTime> reservedTimes = new HashSet<>(activeReservations.stream()
                .map(Reservation::getMoveTime)
                .toList());
        List<LocalTime> availableTimes = java.util.stream.Stream.iterate(
                        schedule.getStartTime(),
                        time -> time.isBefore(schedule.getEndTime()),
                        time -> time.plusMinutes(schedule.getSlotMinutes()))
                .filter(time -> !reservedTimes.contains(time))
                .toList();
        return new AvailabilityResponse(date, false, null, availableTimes);
    }

    public void ensureAvailable(LocalDate date, LocalTime time) {
        ensureAvailable(date, time, null);
    }

    public void ensureAvailable(LocalDate date, LocalTime time, Long excludedReservationId) {
        AvailabilityResponse response = availability(date, excludedReservationId);
        if (response.closed()) {
            throw new ReservationScheduleConflictException(
                    response.closureReason() + "입니다. 다른 날짜를 선택해 주세요.");
        }
        if (!response.availableTimes().contains(time)) {
            throw new ReservationScheduleConflictException(
                    "선택한 날짜와 시간에는 예약할 수 없습니다. 다른 시간을 선택해 주세요.");
        }
    }

    public List<OperatingSchedule> schedules() {
        return scheduleRepository.findAllByOrderByDayOfWeekAsc().stream()
                .sorted(Comparator.comparingInt(schedule -> schedule.getDayOfWeek().getValue()))
                .toList();
    }

    public OperatingPolicy policy() {
        return policyRepository.findById(OperatingPolicy.DEFAULT_ID)
                .orElse(OperatingPolicy.defaults());
    }

    @Transactional
    public OperatingSchedule updateSchedule(Long id, OperatingScheduleUpdateRequest request) {
        if (!request.startTime().isBefore(request.endTime())) {
            throw new IllegalArgumentException("운영 시작 시간은 종료 시간보다 빨라야 합니다.");
        }
        OperatingSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("요일별 운영시간을 찾을 수 없습니다."));
        schedule.update(request.open(), request.startTime(), request.endTime(), request.slotMinutes());
        return schedule;
    }

    @Transactional
    public OperatingPolicy updatePolicy(OperatingPolicyRequest request) {
        if (request.minAdvanceDays() > request.maxAdvanceDays()) {
            throw new IllegalArgumentException("예약 가능 시작일은 종료일보다 작거나 같아야 합니다.");
        }

        OperatingPolicy policy = policyRepository.findById(OperatingPolicy.DEFAULT_ID)
                .orElseGet(() -> policyRepository.save(OperatingPolicy.defaults()));
        policy.update(request.minAdvanceDays(), request.maxAdvanceDays(), request.maxDailyReservations());
        return policy;
    }

    public List<OperatingHoliday> holidays() {
        return holidayRepository.findAllByHolidayDateGreaterThanEqualOrderByHolidayDateAsc(LocalDate.now());
    }

    @Transactional
    public OperatingHoliday addHoliday(OperatingHolidayCreateRequest request) {
        if (holidayRepository.findByHolidayDate(request.holidayDate()).isPresent()) {
            throw new IllegalArgumentException("이미 등록된 휴무일입니다.");
        }
        return holidayRepository.save(new OperatingHoliday(request.holidayDate(), request.reason()));
    }

    @Transactional
    public void deleteHoliday(Long id) {
        if (!holidayRepository.existsById(id)) {
            throw new IllegalArgumentException("휴무일을 찾을 수 없습니다.");
        }
        holidayRepository.deleteById(id);
    }

    @Transactional
    public void initializeDefaults() {
        for (DayOfWeek day : DayOfWeek.values()) {
            if (scheduleRepository.findByDayOfWeek(day).isEmpty()) {
                scheduleRepository.save(new OperatingSchedule(
                        day, day != DayOfWeek.SUNDAY, LocalTime.of(9, 0), LocalTime.of(18, 0), 60
                ));
            }
        }
        if (policyRepository.findById(OperatingPolicy.DEFAULT_ID).isEmpty()) {
            policyRepository.save(OperatingPolicy.defaults());
        }
    }
}
