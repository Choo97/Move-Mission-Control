package com.moving.reservation.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class ReservationEstimateSnapshotLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false)
    private int lineOrder;

    @Column(nullable = false, length = 200)
    private String label;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private LocalDateTime capturedAt;

    protected ReservationEstimateSnapshotLine() {
    }

    public ReservationEstimateSnapshotLine(Reservation reservation,
                                           int lineOrder,
                                           ReservationEstimateLine estimateLine,
                                           LocalDateTime capturedAt) {
        this.reservation = reservation;
        this.lineOrder = lineOrder;
        this.label = estimateLine.label();
        this.amount = estimateLine.amount();
        this.capturedAt = capturedAt;
    }

    public ReservationEstimateLine toEstimateLine() {
        return new ReservationEstimateLine(label, amount);
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public int getLineOrder() {
        return lineOrder;
    }

    public String getLabel() {
        return label;
    }

    public int getAmount() {
        return amount;
    }

    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }
}
