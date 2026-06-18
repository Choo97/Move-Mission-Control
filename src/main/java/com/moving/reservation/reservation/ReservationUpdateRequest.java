package com.moving.reservation.reservation;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "고객 예약 수정 요청")
public class ReservationUpdateRequest {

    @Schema(description = "예약 당시 입력한 연락처. 예약 수정 권한 확인에 사용합니다.", example = "010-1234-5678")
    @NotBlank(message = "연락처를 입력해 주세요.")
    @Pattern(regexp = "^[0-9\\-\\s]+$", message = "연락처는 숫자와 하이픈만 입력해 주세요.")
    private String phone;

    @Schema(description = "수정할 이메일", example = "updated@example.com")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @Schema(description = "수정할 이사 날짜", example = "2026-07-03")
    @NotNull(message = "이사 날짜를 선택해 주세요.")
    @FutureOrPresent(message = "오늘 이후 날짜를 선택해 주세요.")
    private LocalDate moveDate;

    @Schema(description = "수정할 이사 시간", example = "14:00")
    @NotNull(message = "희망 시간을 선택해 주세요.")
    private LocalTime moveTime;

    @Schema(description = "수정할 출발지 주소", example = "서울시 마포구 월드컵북로 1")
    @NotBlank(message = "출발 주소를 입력해 주세요.")
    private String fromAddress;

    @Schema(description = "수정할 도착지 주소", example = "서울시 용산구 한강대로 1")
    @NotBlank(message = "도착 주소를 입력해 주세요.")
    private String toAddress;

    @Schema(description = "수정할 출발지 층수", example = "7")
    @NotNull(message = "출발지 층수를 입력해 주세요.")
    @Min(value = 1, message = "층수는 1층 이상이어야 합니다.")
    @Max(value = 50, message = "층수는 50층 이하로 입력해 주세요.")
    private Integer fromFloor;

    @Schema(description = "수정할 도착지 층수", example = "9")
    @NotNull(message = "도착지 층수를 입력해 주세요.")
    @Min(value = 1, message = "층수는 1층 이상이어야 합니다.")
    @Max(value = 50, message = "층수는 50층 이하로 입력해 주세요.")
    private Integer toFloor;

    @Schema(description = "출발지 사다리차 필요 여부", example = "true")
    private boolean fromLadderTruck;

    @Schema(description = "도착지 사다리차 필요 여부", example = "false")
    private boolean toLadderTruck;

    @Schema(description = "수정할 고객 요청사항", example = "API로 수정한 예약입니다.")
    private String memo;

    public static ReservationUpdateRequest from(Reservation reservation) {
        ReservationUpdateRequest request = new ReservationUpdateRequest();
        request.setMoveDate(reservation.getMoveDate());
        request.setEmail(reservation.getEmail());
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
