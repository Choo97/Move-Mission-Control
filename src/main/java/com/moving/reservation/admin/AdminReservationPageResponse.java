package com.moving.reservation.admin;

import com.moving.reservation.reservation.Reservation;
import java.util.List;
import org.springframework.data.domain.Page;

public record AdminReservationPageResponse(
        List<AdminReservationListItemResponse> content,
        AdminDashboardTaskSummary taskSummary,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static AdminReservationPageResponse from(Page<Reservation> page,
                                                    AdminDashboardTaskSummary taskSummary) {
        return new AdminReservationPageResponse(
                page.getContent().stream()
                        .map(AdminReservationListItemResponse::from)
                        .toList(),
                taskSummary,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
