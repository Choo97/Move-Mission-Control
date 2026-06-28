package com.moving.reservation.admin;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.moving.reservation.reservation.MoveType;
import com.moving.reservation.reservation.ReservationCreateRequest;
import com.moving.reservation.reservation.ReservationService;
import com.moving.reservation.reservation.ReservationStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:admin-api-response-commit-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminReservationApiControllerResponseCommitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationService reservationService;

    @Test
    void 관리자_저장_API는_실제_요청처럼_커밋된_뒤에도_최신_상세를_응답한다() throws Exception {
        var reservation = reservationService.create(reservationCreateRequest());

        mockMvc.perform(patch("/api/admin/reservations/{id}/status", reservation.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CONSULTING"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ReservationStatus.CONSULTING.name()));

        mockMvc.perform(patch("/api/admin/reservations/{id}/distance", reservation.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "distanceKm": 6
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.distanceKm").value(6));

        mockMvc.perform(patch("/api/admin/reservations/{id}/estimate", reservation.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estimatedPrice": 123000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estimatedPrice").value(123000))
                .andExpect(jsonPath("$.status").value(ReservationStatus.ESTIMATE_SENT.name()));

        mockMvc.perform(patch("/api/admin/reservations/{id}/memo", reservation.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "adminMemo": "요청별 트랜잭션 응답 확인"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adminMemo").value("요청별 트랜잭션 응답 확인"))
                .andExpect(jsonPath("$.adminMemoUpdatedBy").value("admin"));
    }

    private ReservationCreateRequest reservationCreateRequest() {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setCustomerName("실제요청고객");
        request.setPhone("010-9999-0099");
        request.setEmail("admin-api-response@example.com");
        request.setMoveDate(LocalDate.now().plusDays(7));
        request.setMoveTime(LocalTime.of(10, 30));
        request.setFromAddress("서울시 강남구 테헤란로 1");
        request.setToAddress("서울시 송파구 올림픽로 1");
        request.setMoveType(MoveType.STUDIO);
        request.setFromElevator(true);
        request.setToElevator(true);
        request.setFromFloor(3);
        request.setToFloor(5);
        request.setFromLadderTruck(false);
        request.setToLadderTruck(false);
        request.setMemo("관리자 API 실제 요청 응답 테스트 예약입니다.");
        return request;
    }
}
