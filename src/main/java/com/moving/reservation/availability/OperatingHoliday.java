package com.moving.reservation.availability;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class OperatingHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate holidayDate;

    @Column(nullable = false, length = 100)
    private String reason;

    protected OperatingHoliday() {
    }

    public OperatingHoliday(LocalDate holidayDate, String reason) {
        this.holidayDate = holidayDate;
        this.reason = reason == null || reason.isBlank() ? "지정 휴무일" : reason.trim();
    }

    public Long getId() { return id; }
    public LocalDate getHolidayDate() { return holidayDate; }
    public String getReason() { return reason; }
}
