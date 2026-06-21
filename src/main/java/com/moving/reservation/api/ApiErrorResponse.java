package com.moving.reservation.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "API 공통 오류 응답")
public record ApiErrorResponse(
        @Schema(description = "오류 코드. 화면에서는 이 값으로 오류 종류를 구분합니다.", example = "BAD_REQUEST")
        String code,
        @Schema(description = "고객 또는 관리자에게 보여줄 오류 안내 문구", example = "예약 번호와 연락처가 일치하지 않습니다.")
        String message
) {

    public static ApiErrorResponse badRequest(String message) {
        return new ApiErrorResponse("BAD_REQUEST", message);
    }

    public static ApiErrorResponse notFound(String message) {
        return new ApiErrorResponse("NOT_FOUND", message);
    }

    public static ApiErrorResponse conflict(String message) {
        return new ApiErrorResponse("RESERVATION_SCHEDULE_CONFLICT", message);
    }
}
