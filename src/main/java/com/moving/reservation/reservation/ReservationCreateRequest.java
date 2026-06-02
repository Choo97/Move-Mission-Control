package com.moving.reservation.reservation;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public class ReservationCreateRequest {

    @NotBlank(message = "이름을 입력해 주세요.")
    private String customerName;

    @NotBlank(message = "연락처를 입력해 주세요.")
    @Pattern(regexp = "^[0-9\\-\\s]+$", message = "연락처는 숫자와 하이픈만 입력해 주세요.")
    private String phone;

    @NotNull(message = "이사 날짜를 선택해 주세요.")
    @FutureOrPresent(message = "오늘 이후 날짜를 선택해 주세요.")
    private LocalDate moveDate;

    @NotNull(message = "희망 시간을 선택해 주세요.")
    private LocalTime moveTime;

    @NotBlank(message = "출발 주소를 입력해 주세요.")
    private String fromAddress;

    @NotBlank(message = "도착 주소를 입력해 주세요.")
    private String toAddress;

    @NotNull(message = "이사 유형을 선택해 주세요.")
    private MoveType moveType;

    private boolean fromElevator;
    private boolean toElevator;
    private String memo;
    private String couponCode;
    private List<MultipartFile> itemPhotos = new ArrayList<>();

    public Reservation toEntity() {
        return new Reservation(
                customerName,
                phone,
                moveDate,
                moveTime,
                fromAddress,
                toAddress,
                moveType,
                fromElevator,
                toElevator,
                memo
        );
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getMoveDate() {
        return moveDate;
    }

    public void setMoveDate(LocalDate moveDate) {
        this.moveDate = moveDate;
    }

    public LocalTime getMoveTime() {
        return moveTime;
    }

    public void setMoveTime(LocalTime moveTime) {
        this.moveTime = moveTime;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public void setMoveType(MoveType moveType) {
        this.moveType = moveType;
    }

    public boolean isFromElevator() {
        return fromElevator;
    }

    public void setFromElevator(boolean fromElevator) {
        this.fromElevator = fromElevator;
    }

    public boolean isToElevator() {
        return toElevator;
    }

    public void setToElevator(boolean toElevator) {
        this.toElevator = toElevator;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public List<MultipartFile> getItemPhotos() {
        return itemPhotos;
    }

    public void setItemPhotos(List<MultipartFile> itemPhotos) {
        this.itemPhotos = itemPhotos;
    }
}
