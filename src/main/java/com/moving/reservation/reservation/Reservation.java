package com.moving.reservation.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String customerName;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false)
    private LocalDate moveDate;

    @Column(nullable = false)
    private LocalTime moveTime;

    @Column(nullable = false, length = 200)
    private String fromAddress;

    @Column(nullable = false, length = 200)
    private String toAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MoveType moveType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status = ReservationStatus.RECEIVED;

    @Column(nullable = false)
    private boolean fromElevator;

    @Column(nullable = false)
    private boolean toElevator;

    @Column(length = 1000)
    private String memo;

    private Integer estimatedPrice;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Reservation() {
    }

    public Reservation(String customerName, String phone, LocalDate moveDate, LocalTime moveTime,
                       String fromAddress, String toAddress, MoveType moveType,
                       boolean fromElevator, boolean toElevator, String memo) {
        this.customerName = customerName;
        this.phone = phone;
        this.moveDate = moveDate;
        this.moveTime = moveTime;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.moveType = moveType;
        this.fromElevator = fromElevator;
        this.toElevator = toElevator;
        this.memo = memo;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(ReservationStatus status) {
        this.status = status;
    }

    public void updateEstimate(Integer estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDate getMoveDate() {
        return moveDate;
    }

    public LocalTime getMoveTime() {
        return moveTime;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public boolean isFromElevator() {
        return fromElevator;
    }

    public boolean isToElevator() {
        return toElevator;
    }

    public String getMemo() {
        return memo;
    }

    public Integer getEstimatedPrice() {
        return estimatedPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
