package com.moving.reservation.reservation;

import com.moving.reservation.privacy.SensitiveStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
public class ReservationConflictAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = SensitiveStringConverter.class)
    @Column(nullable = false, length = 500)
    private String customerName;

    @Convert(converter = SensitiveStringConverter.class)
    @Column(nullable = false, length = 500)
    private String phone;

    @Column(nullable = false)
    private LocalDate moveDate;

    @Column(nullable = false)
    private LocalTime moveTime;

    @Column(nullable = false)
    private LocalDateTime attemptedAt;

    protected ReservationConflictAttempt() {
    }

    public ReservationConflictAttempt(String customerName, String phone, LocalDate moveDate, LocalTime moveTime) {
        this.customerName = customerName;
        this.phone = phone;
        this.moveDate = moveDate;
        this.moveTime = moveTime;
        this.attemptedAt = LocalDateTime.now();
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

    public LocalDateTime getAttemptedAt() {
        return attemptedAt;
    }
}
