package com.moving.reservation.review;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Autowired
    private ReviewService reviewService;

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

    @Test
    void 공개리뷰_API는_공개된_리뷰만_개인정보_없이_조회한다() throws Exception {
        Reservation publicReservation = completedReservation("김민수", "010-1111-1111");
        Review publicReview = reviewService.create(reviewCreateRequest(
                publicReservation.getId(),
                "010-1111-1111",
                5,
                "상담부터 이사 완료까지 친절했습니다."
        ));
        reviewService.updateAdminReply(publicReview.getId(), "이용해 주셔서 감사합니다.", "admin");

        Reservation hiddenReservation = completedReservation("이영희", "010-2222-2222");
        Review hiddenReview = reviewService.create(reviewCreateRequest(
                hiddenReservation.getId(),
                "010-2222-2222",
                4,
                "숨김 처리할 리뷰입니다."
        ));
        reviewService.hide(hiddenReview.getId());

        mockMvc.perform(get("/api/reviews/public?limit=6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(publicReview.getId()))
                .andExpect(jsonPath("$[0].customerName").value("김** 고객"))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[0].content").value("상담부터 이사 완료까지 친절했습니다."))
                .andExpect(jsonPath("$[0].adminReply").value("이용해 주셔서 감사합니다."))
                .andExpect(jsonPath("$[0].moveTypeLabel").value("원룸"))
                .andExpect(jsonPath("$[0].createdAt").isString())
                .andExpect(jsonPath("$[0].reservationId").doesNotExist())
                .andExpect(jsonPath("$[0].phone").doesNotExist())
                .andExpect(jsonPath("$[0].email").doesNotExist())
                .andExpect(jsonPath("$[1]").doesNotExist());
    }

    private Reservation completedReservation(String phone) {
        return completedReservation("리뷰API고객", phone);
    }

    private Reservation completedReservation(String customerName, String phone) {
        Reservation reservation = reservationService.create(reservationCreateRequest(customerName, phone));
        reservationService.updateStatus(reservation.getId(), ReservationStatus.CONSULTING, "test-admin");
        reservationService.updateStatus(reservation.getId(), ReservationStatus.ESTIMATE_SENT, "test-admin");
        reservationService.updateStatus(reservation.getId(), ReservationStatus.CONFIRMED, "test-admin");
        reservationService.updateStatus(reservation.getId(), ReservationStatus.COMPLETED, "test-admin");
        return reservation;
    }

    private ReservationCreateRequest reservationCreateRequest(String phone) {
        return reservationCreateRequest("리뷰API고객", phone);
    }

    private ReservationCreateRequest reservationCreateRequest(String customerName, String phone) {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setCustomerName(customerName);
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

    private ReviewCreateRequest reviewCreateRequest(Long reservationId, String phone, Integer rating, String content) {
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setReservationId(reservationId);
        request.setPhone(phone);
        request.setRating(rating);
        request.setContent(content);
        return request;
    }
}
