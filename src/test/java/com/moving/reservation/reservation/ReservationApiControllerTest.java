package com.moving.reservation.reservation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReservationApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationService reservationService;

    @Test
    void 예약번호와_연락처로_예약을_JSON으로_조회한다() throws Exception {
        Reservation reservation = reservationService.create(reservationCreateRequest("010-1234-5678"));

        mockMvc.perform(post("/api/reservations/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reservationId": %d,
                                  "phone": "010-1234-5678"
                                }
                                """.formatted(reservation.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservation.getId()))
                .andExpect(jsonPath("$.customerName").value("API조회고객"))
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.statusLabel").value("접수"))
                .andExpect(jsonPath("$.moveType").value("STUDIO"))
                .andExpect(jsonPath("$.moveTypeLabel").value("원룸"))
                .andExpect(jsonPath("$.finalEstimatedPrice").isNumber())
                .andExpect(jsonPath("$.estimateLines").isArray());
    }

    @Test
    void 연락처가_다르면_예약조회_API는_404를_응답한다() throws Exception {
        Reservation reservation = reservationService.create(reservationCreateRequest("010-1234-5678"));

        mockMvc.perform(post("/api/reservations/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reservationId": %d,
                                  "phone": "010-0000-0000"
                                }
                                """.formatted(reservation.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("예약 번호와 연락처가 일치하는 예약을 찾을 수 없습니다."));
    }

    @Test
    void 예약조회_API는_CSRF_토큰없이_호출할_수_있다() throws Exception {
        Reservation reservation = reservationService.create(reservationCreateRequest("010-1234-5678"));

        mockMvc.perform(post("/api/reservations/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reservationId": %d,
                                  "phone": "010-1234-5678"
                                }
                                """.formatted(reservation.getId())))
                .andExpect(status().isOk());
    }

    private ReservationCreateRequest reservationCreateRequest(String phone) {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setCustomerName("API조회고객");
        request.setPhone(phone);
        request.setEmail("api-customer@example.com");
        request.setMoveDate(LocalDate.now().plusDays(7));
        request.setMoveTime(LocalTime.of(10, 30));
        request.setFromAddress("서울시 강남구 테헤란로 1");
        request.setToAddress("서울시 송파구 올림픽로 1");
        request.setMoveType(MoveType.STUDIO);
        request.setFromElevator(true);
        request.setToElevator(true);
        request.setFromFloor(3);
        request.setToFloor(5);
        request.setMemo("API 조회 테스트 예약입니다.");
        return request;
    }
}
