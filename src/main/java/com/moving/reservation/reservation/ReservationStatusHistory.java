package com.moving.reservation.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class ReservationStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus changedStatus;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @Column(length = 50)
    private String changedBy;

    protected ReservationStatusHistory() {
    }

    public ReservationStatusHistory(Reservation reservation, ReservationStatus previousStatus,
                                    ReservationStatus changedStatus, String changedBy) {
        this.reservation = reservation;
        this.previousStatus = previousStatus;
        this.changedStatus = changedStatus;
        this.changedAt = LocalDateTime.now();
        this.changedBy = changedBy;
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public ReservationStatus getPreviousStatus() {
        return previousStatus;
    }

    public ReservationStatus getChangedStatus() {
        return changedStatus;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }
}
