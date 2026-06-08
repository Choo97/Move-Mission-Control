package com.moving.reservation.reservation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CustomerGuideSaveRequest {

    @NotNull(message = "예약 상태를 선택해 주세요.")
    private ReservationStatus status;

    @NotBlank(message = "제목을 입력해 주세요.")
    @Size(max = 80, message = "제목은 80자 이하로 입력해 주세요.")
    private String title;

    @NotBlank(message = "설명을 입력해 주세요.")
    @Size(max = 500, message = "설명은 500자 이하로 입력해 주세요.")
    private String description;

    @NotNull(message = "정렬 순서를 입력해 주세요.")
    @Min(value = 1, message = "정렬 순서는 1 이상이어야 합니다.")
    private Integer displayOrder;

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
