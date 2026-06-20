package com.moving.reservation.admin;

import com.moving.reservation.reservation.ReservationStatus;
import jakarta.validation.constraints.NotNull;

public class AdminReservationStatusUpdateRequest {

    @NotNull(message = "변경할 예약 상태를 선택해 주세요.")
    private ReservationStatus status;

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
}
