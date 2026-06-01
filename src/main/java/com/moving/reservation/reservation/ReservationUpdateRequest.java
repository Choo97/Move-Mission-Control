package com.moving.reservation.reservation;

import jakarta.validation.constraints.FutureOrPresent;
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

    private String memo;

    public static ReservationUpdateRequest from(Reservation reservation) {
        ReservationUpdateRequest request = new ReservationUpdateRequest();
        request.setMoveDate(reservation.getMoveDate());
        request.setMoveTime(reservation.getMoveTime());
        request.setFromAddress(reservation.getFromAddress());
        request.setToAddress(reservation.getToAddress());
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

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
