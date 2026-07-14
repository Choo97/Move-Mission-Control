package com.moving.reservation.reservation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "고객 예약 신청 요청")
public class ReservationApiCreateRequest {

    @Schema(description = "예약자 이름", example = "홍길동")
    @NotBlank(message = "이름을 입력해 주세요.")
    private String customerName;

    @Schema(description = "예약자 연락처. 예약 조회, 수정, 취소 시 본인 확인에 사용합니다.", example = "010-1234-5678")
    @NotBlank(message = "연락처를 입력해 주세요.")
    @Pattern(regexp = "^[0-9\\-\\s]+$", message = "연락처는 숫자와 하이픈만 입력해 주세요.")
    private String phone;

    @Schema(description = "예약 확인 안내를 받을 이메일", example = "customer@example.com")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @Schema(description = "희망 이사 날짜", example = "2026-07-01")
    @NotNull(message = "이사 날짜를 선택해 주세요.")
    @FutureOrPresent(message = "오늘 이후 날짜를 선택해 주세요.")
    private LocalDate moveDate;

    @Schema(description = "희망 이사 시간", example = "10:30")
    @NotNull(message = "희망 시간을 선택해 주세요.")
    private LocalTime moveTime;

    @Schema(description = "출발지 주소", example = "서울시 강남구 테헤란로 1")
    @NotBlank(message = "출발 주소를 입력해 주세요.")
    private String fromAddress;

    @Schema(description = "도착지 주소", example = "서울시 송파구 올림픽로 1")
    @NotBlank(message = "도착 주소를 입력해 주세요.")
    private String toAddress;

    @Schema(description = "이사 유형", example = "STUDIO")
    @NotNull(message = "이사 유형을 선택해 주세요.")
    private MoveType moveType;

    @Schema(description = "요청 서비스 유형", example = "GENERAL")
    @NotNull(message = "서비스 유형을 선택해 주세요.")
    private ServiceRequestType serviceType = ServiceRequestType.GENERAL;

    @Schema(description = "출발지 엘리베이터 사용 가능 여부", example = "true")
    private boolean fromElevator;

    @Schema(description = "도착지 엘리베이터 사용 가능 여부", example = "true")
    private boolean toElevator;

    @Schema(description = "출발지 층수", example = "3")
    @NotNull(message = "출발지 층수를 입력해 주세요.")
    @Min(value = 1, message = "층수는 1층 이상이어야 합니다.")
    @Max(value = 50, message = "층수는 50층 이하로 입력해 주세요.")
    private Integer fromFloor = 1;

    @Schema(description = "도착지 층수", example = "5")
    @NotNull(message = "도착지 층수를 입력해 주세요.")
    @Min(value = 1, message = "층수는 1층 이상이어야 합니다.")
    @Max(value = 50, message = "층수는 50층 이하로 입력해 주세요.")
    private Integer toFloor = 1;

    @Schema(description = "출발지 사다리차 필요 여부", example = "false")
    private boolean fromLadderTruck;

    @Schema(description = "도착지 사다리차 필요 여부", example = "false")
    private boolean toLadderTruck;

    @Schema(description = "고객 요청사항", example = "깨지기 쉬운 짐이 있습니다.")
    private String memo;

    @Schema(description = "사용할 쿠폰 코드", example = "WELCOME10")
    private String couponCode;

    public ReservationCreateRequest toServiceRequest() {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setCustomerName(customerName);
        request.setPhone(phone);
        request.setEmail(email);
        request.setMoveDate(moveDate);
        request.setMoveTime(moveTime);
        request.setFromAddress(fromAddress);
        request.setToAddress(toAddress);
        request.setMoveType(moveType);
        request.setServiceType(serviceType);
        request.setFromElevator(fromElevator);
        request.setToElevator(toElevator);
        request.setFromFloor(fromFloor);
        request.setToFloor(toFloor);
        request.setFromLadderTruck(fromLadderTruck);
        request.setToLadderTruck(toLadderTruck);
        request.setMemo(memo);
        request.setCouponCode(couponCode);
        return request;
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

    public MoveType getMoveType() {
        return moveType;
    }

    public void setMoveType(MoveType moveType) {
        this.moveType = moveType;
    }

    public ServiceRequestType getServiceType() {
        return serviceType == null ? ServiceRequestType.GENERAL : serviceType;
    }

    public void setServiceType(ServiceRequestType serviceType) {
        this.serviceType = serviceType;
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

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
}
