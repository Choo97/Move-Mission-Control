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
public class ReservationCustomerActionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CustomerActionType actionType;

    @Column(nullable = false, length = 200)
    private String summary;

    @Column(nullable = false, length = 2000)
    private String detail;

    @Column(nullable = false, length = 50)
    private String requestedBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected ReservationCustomerActionHistory() {
    }

    public ReservationCustomerActionHistory(Reservation reservation,
                                            CustomerActionType actionType,
                                            String summary,
                                            String detail,
                                            String requestedBy) {
        this.reservation = reservation;
        this.actionType = actionType;
        this.summary = summary;
        this.detail = detail;
        this.requestedBy = requestedBy;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public CustomerActionType getActionType() {
        return actionType;
    }

    public String getSummary() {
        return summary;
    }

    public String getDetail() {
        return detail;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
