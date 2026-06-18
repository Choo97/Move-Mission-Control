package com.moving.reservation.review;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReviewApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationService reservationService;

    @Test
    void 리뷰작성_API로_완료된_예약에_리뷰를_작성한다() throws Exception {
        Reservation reservation = completedReservation("010-1234-5678");

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reservationId": %d,
                                  "phone": "010-1234-5678",
                                  "rating": 5,
                                  "content": "친절하고 정확했습니다."
                                }
                                """.formatted(reservation.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.reservationId").value(reservation.getId()))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.content").value("친절하고 정확했습니다."));
    }

    @Test
    void 리뷰작성_API는_완료되지_않은_예약이면_400을_응답한다() throws Exception {
        Reservation reservation = reservationService.create(reservationCreateRequest("010-1234-5678"));

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reservationId": %d,
                                  "phone": "010-1234-5678",
                                  "rating": 5,
                                  "content": "아직 완료 전입니다."
                                }
                                """.formatted(reservation.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("완료된 예약만 리뷰를 작성할 수 있습니다."));
    }

    @Test
    void 리뷰작성_API는_연락처가_다르면_400을_응답한다() throws Exception {
        Reservation reservation = completedReservation("010-1234-5678");

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reservationId": %d,
                                  "phone": "010-0000-0000",
                                  "rating": 5,
                                  "content": "연락처가 다릅니다."
                                }
                                """.formatted(reservation.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("예약 번호와 연락처가 일치하지 않습니다."));
    }

    @Test
    void 리뷰작성_API는_CSRF_토큰없이_호출할_수_있다() throws Exception {
        Reservation reservation = completedReservation("010-1234-5678");

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reservationId": %d,
                                  "phone": "010-1234-5678",
                                  "rating": 5,
                                  "content": "CSRF 없이 작성합니다."
                                }
                                """.formatted(reservation.getId())))
                .andExpect(status().isCreated());
    }

    private Reservation completedReservation(String phone) {
        Reservation reservation = reservationService.create(reservationCreateRequest(phone));
        reservationService.updateStatus(reservation.getId(), ReservationStatus.CONSULTING, "test-admin");
        reservationService.updateStatus(reservation.getId(), ReservationStatus.ESTIMATE_SENT, "test-admin");
        reservationService.updateStatus(reservation.getId(), ReservationStatus.CONFIRMED, "test-admin");
        reservationService.updateStatus(reservation.getId(), ReservationStatus.COMPLETED, "test-admin");
        return reservation;
    }

    private ReservationCreateRequest reservationCreateRequest(String phone) {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setCustomerName("리뷰API고객");
        request.setPhone(phone);
        request.setEmail("review-api@example.com");
        request.setMoveDate(LocalDate.now().plusDays(7));
        request.setMoveTime(LocalTime.of(10, 30));
        request.setFromAddress("서울시 강남구 테헤란로 1");
        request.setToAddress("서울시 송파구 올림픽로 1");
        request.setMoveType(MoveType.STUDIO);
        request.setFromElevator(true);
        request.setToElevator(true);
        request.setFromFloor(3);
        request.setToFloor(5);
        request.setMemo("리뷰 API 테스트 예약입니다.");
        return request;
    }
}
