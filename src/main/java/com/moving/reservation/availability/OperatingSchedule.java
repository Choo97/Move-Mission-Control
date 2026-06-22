package com.moving.reservation.availability;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
public class OperatingSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private boolean open;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private int slotMinutes;

    protected OperatingSchedule() {
    }

    public OperatingSchedule(DayOfWeek dayOfWeek, boolean open,
                             LocalTime startTime, LocalTime endTime, int slotMinutes) {
        this.dayOfWeek = dayOfWeek;
        update(open, startTime, endTime, slotMinutes);
    }

    public void update(boolean open, LocalTime startTime, LocalTime endTime, int slotMinutes) {
        this.open = open;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotMinutes = slotMinutes;
    }

    public Long getId() { return id; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public boolean isOpen() { return open; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public int getSlotMinutes() { return slotMinutes; }
}
