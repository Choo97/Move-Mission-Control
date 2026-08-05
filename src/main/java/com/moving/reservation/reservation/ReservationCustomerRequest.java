package com.moving.reservation.reservation;

import com.moving.reservation.privacy.SensitiveStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
public class ReservationCustomerRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CustomerRequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CustomerRequestStatus status;

    @Convert(converter = SensitiveStringConverter.class)
    @Column(length = 500)
    private String email;

    private LocalDate moveDate;

    private LocalTime moveTime;

    @Convert(converter = SensitiveStringConverter.class)
    @Column(length = 1000)
    private String fromAddress;

    @Convert(converter = SensitiveStringConverter.class)
    @Column(length = 1000)
    private String toAddress;

    private Integer fromFloor;

    private Integer toFloor;

    private boolean fromLadderTruck;

    private boolean toLadderTruck;

    @Convert(converter = SensitiveStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String memo;

    @Convert(converter = SensitiveStringConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String detail;

    @Convert(converter = SensitiveStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(nullable = false, length = 50)
    private String requestedBy;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    @Column(length = 50)
    private String processedBy;

    private LocalDateTime processedAt;

    protected ReservationCustomerRequest() {
    }

    private ReservationCustomerRequest(Reservation reservation,
                                       CustomerRequestType requestType,
                                       ReservationUpdateRequest updateRequest,
                                       String detail) {
        this.reservation = reservation;
        this.requestType = requestType;
        this.status = CustomerRequestStatus.PENDING;
        this.detail = detail;
        this.requestedBy = "customer";
        this.requestedAt = LocalDateTime.now();

        if (updateRequest != null) {
            this.email = normalizeEmail(updateRequest.getEmail());
            this.moveDate = updateRequest.getMoveDate();
            this.moveTime = updateRequest.getMoveTime();
            this.fromAddress = updateRequest.getFromAddress();
            this.toAddress = updateRequest.getToAddress();
            this.fromFloor = updateRequest.getFromFloor();
            this.toFloor = updateRequest.getToFloor();
            this.fromLadderTruck = updateRequest.isFromLadderTruck();
            this.toLadderTruck = updateRequest.isToLadderTruck();
            this.memo = updateRequest.getMemo();
        }
    }

    public static ReservationCustomerRequest update(Reservation reservation,
                                                    ReservationUpdateRequest updateRequest,
                                                    String detail) {
        return new ReservationCustomerRequest(reservation, CustomerRequestType.UPDATE, updateRequest, detail);
    }

    public static ReservationCustomerRequest cancel(Reservation reservation, String detail) {
        return new ReservationCustomerRequest(reservation, CustomerRequestType.CANCEL, null, detail);
    }

    public void approve(String processedBy) {
        ensurePending();
        this.status = CustomerRequestStatus.APPROVED;
        this.processedBy = processedBy;
        this.processedAt = LocalDateTime.now();
        this.rejectionReason = null;
    }

    public void reject(String processedBy, String rejectionReason) {
        ensurePending();
        this.status = CustomerRequestStatus.REJECTED;
        this.processedBy = processedBy;
        this.processedAt = LocalDateTime.now();
        this.rejectionReason = rejectionReason;
    }

    private void ensurePending() {
        if (status != CustomerRequestStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 고객 요청입니다.");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim();
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public CustomerRequestType getRequestType() {
        return requestType;
    }

    public CustomerRequestStatus getStatus() {
        return status;
    }

    public String getEmail() {
        return email;
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

    public Integer getFromFloor() {
        return fromFloor;
    }

    public Integer getToFloor() {
        return toFloor;
    }

    public boolean isFromLadderTruck() {
        return fromLadderTruck;
    }

    public boolean isToLadderTruck() {
        return toLadderTruck;
    }

    public String getMemo() {
        return memo;
    }

    public String getDetail() {
        return detail;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}
