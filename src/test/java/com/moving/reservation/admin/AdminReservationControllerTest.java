package com.moving.reservation.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.moving.reservation.reservation.MoveType;
import com.moving.reservation.reservation.Reservation;
import com.moving.reservation.reservation.ReservationCreateRequest;
import com.moving.reservation.reservation.ReservationService;
import com.moving.reservation.reservation.ReservationStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private AdminAuditLogService adminAuditLogService;

    @Test
    void 관리자_예약목록_화면을_조회한다() throws Exception {
        reservationService.create(reservationCreateRequest("010-1234-5678"));

        mockMvc.perform(get("/admin/reservations")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reservations"))
                .andExpect(model().attributeExists(
                        "reservations",
                        "reservationPage",
                        "summary",
                        "taskSummary",
                        "statuses"
                ));
    }

    @Test
    void 관리자_예약상세_화면을_조회한다() throws Exception {
        Reservation reservation = reservationService.create(reservationCreateRequest("010-1234-5678"));

        mockMvc.perform(get("/admin/reservations/{id}", reservation.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reservation-detail"))
                .andExpect(model().attribute("reservation", reservation))
                .andExpect(model().attributeExists(
                        "estimateLines",
                        "photos",
                        "statusHistories",
                        "customerActionHistories",
                        "notifications",
                        "auditLogs",
                        "statuses"
                ));
    }

    @Test
    void 관리자_달력_화면을_조회한다() throws Exception {
        LocalDate moveDate = LocalDate.now().plusDays(3);
        reservationService.create(reservationCreateRequest("010-1234-5678", moveDate));

        mockMvc.perform(get("/admin/calendar")
                        .param("month", java.time.YearMonth.from(moveDate).toString())
                        .param("date", moveDate.toString())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/calendar"))
                .andExpect(model().attributeExists(
                        "selectedMonth",
                        "selectedDate",
                        "weeks",
                        "selectedReservations",
                        "todayReservations",
                        "monthReservationCount",
                        "calendarReturnUrl"
                ));
    }

    @Test
    void 관리자_상태변경_요청은_상태를_변경하고_감사로그를_남긴다() throws Exception {
        Reservation reservation = reservationService.create(reservationCreateRequest("010-1234-5678"));

        mockMvc.perform(post("/admin/reservations/{id}/status", reservation.getId())
                        .param("status", ReservationStatus.CONSULTING.name())
                        .with(csrf())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reservations/" + reservation.getId()));

        assertThat(reservationService.get(reservation.getId()).getStatus()).isEqualTo(ReservationStatus.CONSULTING);
        assertThat(adminAuditLogService.findByReservationId(reservation.getId()))
                .singleElement()
                .satisfies(auditLog -> {
                    assertThat(auditLog.getAction()).isEqualTo("예약 상태 변경");
                    assertThat(auditLog.getDetail()).contains("상담중");
                    assertThat(auditLog.getCreatedBy()).isEqualTo("admin");
                });
    }

    private ReservationCreateRequest reservationCreateRequest(String phone) {
        return reservationCreateRequest(phone, LocalDate.now().plusDays(7));
    }

    private ReservationCreateRequest reservationCreateRequest(String phone, LocalDate moveDate) {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setCustomerName("관리자화면고객");
        request.setPhone(phone);
        request.setEmail("admin-view@example.com");
        request.setMoveDate(moveDate);
        request.setMoveTime(LocalTime.of(10, 30));
        request.setFromAddress("서울시 강남구 테헤란로 1");
        request.setToAddress("서울시 송파구 올림픽로 1");
        request.setMoveType(MoveType.STUDIO);
        request.setFromElevator(true);
        request.setToElevator(true);
        request.setFromFloor(3);
        request.setToFloor(5);
        request.setMemo("관리자 화면 테스트 예약입니다.");
        return request;
    }
}
