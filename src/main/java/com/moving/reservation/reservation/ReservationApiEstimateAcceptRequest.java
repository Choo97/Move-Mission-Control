package com.moving.reservation.reservation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ReservationApiEstimateAcceptRequest {

    @NotBlank(message = "연락처를 입력해 주세요.")
    @Pattern(regexp = "^[0-9\\-\\s]+$", message = "연락처는 숫자와 하이픈만 입력해 주세요.")
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
