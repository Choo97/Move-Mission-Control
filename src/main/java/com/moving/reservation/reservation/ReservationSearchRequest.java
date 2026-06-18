package com.moving.reservation.reservation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "고객 예약 조회 요청")
public class ReservationSearchRequest {

    @Schema(description = "조회할 예약 번호", example = "1")
    @NotNull(message = "예약 번호를 입력해 주세요.")
    private Long reservationId;

    @Schema(description = "예약 당시 입력한 연락처", example = "010-1234-5678")
    @NotBlank(message = "연락처를 입력해 주세요.")
    @Pattern(regexp = "^[0-9\\-\\s]+$", message = "연락처는 숫자와 하이픈만 입력해 주세요.")
    private String phone;

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
