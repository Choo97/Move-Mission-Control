package com.moving.reservation.reservation;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationUpdateRequest {

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

    @NotNull(message = "출발지 층수를 입력해 주세요.")
    @Min(value = 1, message = "층수는 1층 이상이어야 합니다.")
    @Max(value = 50, message = "층수는 50층 이하로 입력해 주세요.")
    private Integer fromFloor;

    @NotNull(message = "도착지 층수를 입력해 주세요.")
    @Min(value = 1, message = "층수는 1층 이상이어야 합니다.")
    @Max(value = 50, message = "층수는 50층 이하로 입력해 주세요.")
    private Integer toFloor;

    private boolean fromLadderTruck;
    private boolean toLadderTruck;

    private String memo;

    public static ReservationUpdateRequest from(Reservation reservation) {
        ReservationUpdateRequest request = new ReservationUpdateRequest();
        request.setMoveDate(reservation.getMoveDate());
        request.setMoveTime(reservation.getMoveTime());
        request.setFromAddress(reservation.getFromAddress());
        request.setToAddress(reservation.getToAddress());
        request.setFromFloor(reservation.getFromFloor());
        request.setToFloor(reservation.getToFloor());
        request.setFromLadderTruck(reservation.isFromLadderTruck());
        request.setToLadderTruck(reservation.isToLadderTruck());
        request.setMemo(reservation.getMemo());
        return request;
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

    public Integer getFromFloor() {
        return fromFloor;
    }

    public void setFromFloor(Integer fromFloor) {
        this.fromFloor = fromFloor;
    }

    public Integer getToFloor() {
        return toFloor;
    }

    public void setToFloor(Integer toFloor) {
        this.toFloor = toFloor;
    }

    public boolean isFromLadderTruck() {
        return fromLadderTruck;
    }

    public void setFromLadderTruck(boolean fromLadderTruck) {
        this.fromLadderTruck = fromLadderTruck;
    }

    public boolean isToLadderTruck() {
        return toLadderTruck;
    }

    public void setToLadderTruck(boolean toLadderTruck) {
        this.toLadderTruck = toLadderTruck;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
