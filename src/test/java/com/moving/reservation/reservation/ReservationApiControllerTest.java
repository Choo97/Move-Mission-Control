package com.moving.reservation.reservation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    void 예약신청_API로_예약을_JSON으로_생성한다() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "API신청고객",
                                  "phone": "010-2222-3333",
                                  "email": "api-create@example.com",
                                  "moveDate": "%s",
                                  "moveTime": "10:30",
                                  "fromAddress": "서울시 강남구 테헤란로 1",
                                  "toAddress": "서울시 송파구 올림픽로 1",
                                  "moveType": "STUDIO",
                                  "fromElevator": true,
                                  "toElevator": true,
                                  "fromFloor": 3,
                                  "toFloor": 5,
                                  "fromLadderTruck": false,
                                  "toLadderTruck": false,
                                  "memo": "API 예약 신청 테스트입니다."
                                }
                                """.formatted(LocalDate.now().plusDays(7))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.customerName").value("API신청고객"))
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.statusLabel").value("접수"))
                .andExpect(jsonPath("$.moveType").value("STUDIO"))
                .andExpect(jsonPath("$.finalEstimatedPrice").isNumber())
                .andExpect(jsonPath("$.estimateLines").isArray());
    }

    @Test
    void 예약신청_API는_필수값이_없으면_400을_응답한다() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "010-2222-3333",
                                  "moveDate": "%s",
                                  "moveTime": "10:30",
                                  "fromAddress": "서울시 강남구 테헤란로 1",
                                  "toAddress": "서울시 송파구 올림픽로 1",
                                  "moveType": "STUDIO"
                                }
                                """.formatted(LocalDate.now().plusDays(7))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이름을 입력해 주세요."));
    }

    @Test
    void 예약신청_API는_CSRF_토큰없이_호출할_수_있다() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "API신청고객",
                                  "phone": "010-2222-3333",
                                  "email": "api-create@example.com",
                                  "moveDate": "%s",
                                  "moveTime": "10:30",
                                  "fromAddress": "서울시 강남구 테헤란로 1",
                                  "toAddress": "서울시 송파구 올림픽로 1",
                                  "moveType": "STUDIO",
                                  "fromElevator": true,
                                  "toElevator": true,
                                  "fromFloor": 3,
                                  "toFloor": 5
                                }
                                """.formatted(LocalDate.now().plusDays(7))))
                .andExpect(status().isCreated());
    }

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

    @Test
    void 예약수정_API로_예약정보를_JSON으로_수정한다() throws Exception {
        Reservation reservation = reservationService.create(reservationCreateRequest("010-1234-5678"));

        mockMvc.perform(patch("/api/reservations/{id}", reservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "010-1234-5678",
                                  "email": "updated-api@example.com",
                                  "moveDate": "%s",
                                  "moveTime": "14:00",
                                  "fromAddress": "서울시 마포구 월드컵북로 1",
                                  "toAddress": "서울시 용산구 한강대로 1",
                                  "fromFloor": 7,
                                  "toFloor": 9,
                                  "fromLadderTruck": true,
                                  "toLadderTruck": false,
                                  "memo": "API로 수정한 예약입니다."
                                }
                                """.formatted(LocalDate.now().plusDays(10))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservation.getId()))
                .andExpect(jsonPath("$.email").value("updated-api@example.com"))
                .andExpect(jsonPath("$.moveTime").value("14:00:00"))
                .andExpect(jsonPath("$.fromAddress").value("서울시 마포구 월드컵북로 1"))
                .andExpect(jsonPath("$.fromLadderTruck").value(true));
    }

    @Test
    void 예약수정_API는_연락처가_다르면_400을_응답한다() throws Exception {
        Reservation reservation = reservationService.create(reservationCreateRequest("010-1234-5678"));

        mockMvc.perform(patch("/api/reservations/{id}", reservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "010-0000-0000",
                                  "email": "updated-api@example.com",
                                  "moveDate": "%s",
                                  "moveTime": "14:00",
                                  "fromAddress": "서울시 마포구 월드컵북로 1",
                                  "toAddress": "서울시 용산구 한강대로 1",
                                  "fromFloor": 7,
                                  "toFloor": 9,
                                  "memo": "API로 수정한 예약입니다."
                                }
                                """.formatted(LocalDate.now().plusDays(10))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("예약 번호와 연락처가 일치하지 않습니다."));
    }

    @Test
    void 예약수정_API는_CSRF_토큰없이_호출할_수_있다() throws Exception {
        Reservation reservation = reservationService.create(reservationCreateRequest("010-1234-5678"));

        mockMvc.perform(patch("/api/reservations/{id}", reservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "010-1234-5678",
                                  "email": "updated-api@example.com",
                                  "moveDate": "%s",
                                  "moveTime": "14:00",
                                  "fromAddress": "서울시 마포구 월드컵북로 1",
                                  "toAddress": "서울시 용산구 한강대로 1",
                                  "fromFloor": 7,
                                  "toFloor": 9
                                }
                                """.formatted(LocalDate.now().plusDays(10))))
                .andExpect(status().isOk());
    }

    @Test
    void 예약취소_API로_예약을_취소한다() throws Exception {
        Reservation reservation = reservationService.create(reservationCreateRequest("010-1234-5678"));

        mockMvc.perform(post("/api/reservations/{id}/cancel", reservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "010-1234-5678"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservation.getId()))
                .andExpect(jsonPath("$.status").value("CANCELED"))
                .andExpect(jsonPath("$.statusLabel").value("취소"))
                .andExpect(jsonPath("$.cancelable").value(false));
    }

    @Test
    void 예약취소_API는_연락처가_다르면_400을_응답한다() throws Exception {
        Reservation reservation = reservationService.create(reservationCreateRequest("010-1234-5678"));

        mockMvc.perform(post("/api/reservations/{id}/cancel", reservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "010-0000-0000"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("예약 번호와 연락처가 일치하지 않습니다."));
    }

    @Test
    void 예약취소_API는_CSRF_토큰없이_호출할_수_있다() throws Exception {
        Reservation reservation = reservationService.create(reservationCreateRequest("010-1234-5678"));

        mockMvc.perform(post("/api/reservations/{id}/cancel", reservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phone": "010-1234-5678"
                                }
                                """))
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
