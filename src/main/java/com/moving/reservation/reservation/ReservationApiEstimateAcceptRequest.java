package com.moving.reservation.reservation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "고객 견적 동의 요청")
public class ReservationApiEstimateAcceptRequest {

    @Schema(description = "예약 당시 입력한 연락처. 견적 동의 권한 확인에 사용합니다.", example = "010-1234-5678")
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
