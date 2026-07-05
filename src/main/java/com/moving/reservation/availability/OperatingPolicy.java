package com.moving.reservation.availability;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

@Entity
public class OperatingPolicy {

    public static final Long DEFAULT_ID = 1L;

    @Id
    private Long id;

    @Column(nullable = false)
    private int minAdvanceDays;

    @Column(nullable = false)
    private int maxAdvanceDays;

    @Column(nullable = false)
    private int maxDailyReservations;

    @Enumerated(EnumType.STRING)
    private OperatingServiceMode serviceMode = OperatingServiceMode.GENERAL;

    protected OperatingPolicy() {
    }

    private OperatingPolicy(Long id, int minAdvanceDays, int maxAdvanceDays, int maxDailyReservations) {
        this.id = id;
        update(minAdvanceDays, maxAdvanceDays, maxDailyReservations);
    }

    public static OperatingPolicy defaults() {
        return new OperatingPolicy(DEFAULT_ID, 0, 90, 8);
    }

    public void update(int minAdvanceDays, int maxAdvanceDays, int maxDailyReservations) {
        update(minAdvanceDays, maxAdvanceDays, maxDailyReservations, getServiceMode());
    }

    public void update(int minAdvanceDays, int maxAdvanceDays, int maxDailyReservations,
                       OperatingServiceMode serviceMode) {
        this.minAdvanceDays = minAdvanceDays;
        this.maxAdvanceDays = maxAdvanceDays;
        this.maxDailyReservations = maxDailyReservations;
        this.serviceMode = serviceMode == null ? OperatingServiceMode.GENERAL : serviceMode;
    }

    public Long getId() {
        return id;
    }

    public int getMinAdvanceDays() {
        return minAdvanceDays;
    }

    public int getMaxAdvanceDays() {
        return maxAdvanceDays;
    }

    public int getMaxDailyReservations() {
        return maxDailyReservations;
    }

    public OperatingServiceMode getServiceMode() {
        return serviceMode == null ? OperatingServiceMode.GENERAL : serviceMode;
    }
}
