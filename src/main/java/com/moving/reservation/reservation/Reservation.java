package com.moving.reservation.reservation;

import com.moving.reservation.coupon.Coupon;
import com.moving.reservation.coupon.DiscountType;
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

    @Column(length = 1000)
    private String adminMemo;

    @Column(length = 50)
    private String adminMemoUpdatedBy;

    private Integer estimatedPrice;

    private Integer baseEstimatedPrice;

    @Column(length = 40)
    private String couponCode;

    @Column(length = 100)
    private String couponName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DiscountType couponDiscountType;

    private Integer couponDiscountValue;

    private Integer discountAmount;

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
        recalculateDiscount();
    }

    public void applyBaseEstimate(Integer baseEstimatedPrice) {
        this.baseEstimatedPrice = baseEstimatedPrice;

        if (this.estimatedPrice == null) {
            updateEstimate(baseEstimatedPrice);
        }
    }

    public void updateAdminMemo(String adminMemo, String adminMemoUpdatedBy) {
        this.adminMemo = adminMemo;
        this.adminMemoUpdatedBy = adminMemoUpdatedBy;
    }

    public boolean isCancelable() {
        return status == ReservationStatus.RECEIVED || status == ReservationStatus.CONSULTING;
    }

    public boolean isEditable() {
        return status == ReservationStatus.RECEIVED || status == ReservationStatus.CONSULTING;
    }

    public void updateDetails(LocalDate moveDate, LocalTime moveTime, String fromAddress, String toAddress, String memo) {
        this.moveDate = moveDate;
        this.moveTime = moveTime;
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.memo = memo;
    }

    public void applyCoupon(Coupon coupon) {
        this.couponCode = coupon.getCode();
        this.couponName = coupon.getName();
        this.couponDiscountType = coupon.getDiscountType();
        this.couponDiscountValue = coupon.getDiscountValue();
        recalculateDiscount();
    }

    public Integer getFinalEstimatedPrice() {
        if (estimatedPrice == null) {
            return null;
        }

        return Math.max(0, estimatedPrice - getAppliedDiscountAmount());
    }

    public int getAppliedDiscountAmount() {
        return discountAmount == null ? 0 : discountAmount;
    }

    public boolean hasCoupon() {
        return couponCode != null && !couponCode.isBlank();
    }

    private void recalculateDiscount() {
        if (estimatedPrice == null || estimatedPrice <= 0 || couponDiscountType == null || couponDiscountValue == null) {
            this.discountAmount = 0;
            return;
        }

        int discount = switch (couponDiscountType) {
            case FIXED -> couponDiscountValue;
            case PERCENT -> estimatedPrice * couponDiscountValue / 100;
        };

        this.discountAmount = Math.min(estimatedPrice, discount);
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

    public String getAdminMemo() {
        return adminMemo;
    }

    public String getAdminMemoUpdatedBy() {
        return adminMemoUpdatedBy;
    }

    public Integer getEstimatedPrice() {
        return estimatedPrice;
    }

    public Integer getBaseEstimatedPrice() {
        return baseEstimatedPrice;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public String getCouponName() {
        return couponName;
    }

    public DiscountType getCouponDiscountType() {
        return couponDiscountType;
    }

    public Integer getCouponDiscountValue() {
        return couponDiscountValue;
    }

    public Integer getDiscountAmount() {
        return discountAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
