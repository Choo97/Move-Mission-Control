package com.moving.reservation.availability;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AvailabilityApiController {

    private final AvailabilityService availabilityService;

    public AvailabilityApiController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping("/api/availability")
    public AvailabilityResponse availability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return availabilityService.availability(date);
    }

    @GetMapping("/api/admin/operating-schedules")
    public List<OperatingScheduleResponse> schedules() {
        return availabilityService.schedules().stream().map(OperatingScheduleResponse::from).toList();
    }

    @PutMapping("/api/admin/operating-schedules/{id}")
    public OperatingScheduleResponse updateSchedule(@PathVariable Long id,
                                                    @Valid @RequestBody OperatingScheduleUpdateRequest request) {
        return OperatingScheduleResponse.from(availabilityService.updateSchedule(id, request));
    }

    @GetMapping("/api/admin/holidays")
    public List<OperatingHolidayResponse> holidays() {
        return availabilityService.holidays().stream().map(OperatingHolidayResponse::from).toList();
    }

    @PostMapping("/api/admin/holidays")
    public OperatingHolidayResponse addHoliday(@Valid @RequestBody OperatingHolidayCreateRequest request) {
        return OperatingHolidayResponse.from(availabilityService.addHoliday(request));
    }

    @DeleteMapping("/api/admin/holidays/{id}")
    public void deleteHoliday(@PathVariable Long id) {
        availabilityService.deleteHoliday(id);
    }
}
