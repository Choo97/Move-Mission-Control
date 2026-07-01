package com.moving.reservation.review;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class AdminReviewApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReviewService reviewService;

    @Test
    void 관리자_리뷰_API는_로그인이_필요하다() throws Exception {
        mockMvc.perform(get("/api/admin/reviews"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 관리자_리뷰_API는_리뷰와_예약정보를_조회한다() throws Exception {
        Reservation reservation = completedReservation("리뷰관리고객", "010-1234-5678");
        reviewService.create(reviewCreateRequest(reservation.getId(), "010-1234-5678", 5, "친절하고 정확했습니다."));

        mockMvc.perform(get("/api/admin/reviews")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].reservationId").value(reservation.getId()))
                .andExpect(jsonPath("$[0].customerName").value("리**"))
                .andExpect(jsonPath("$[0].phone").value("010-****-5678"))
                .andExpect(jsonPath("$[0].email").value("a***@example.com"))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[0].content").value("친절하고 정확했습니다."))
                .andExpect(jsonPath("$[0].published").value(true))
                .andExpect(jsonPath("$[0].moveDate").isString())
                .andExpect(jsonPath("$[0].moveTime").isString())
                .andExpect(jsonPath("$[0].status").value(ReservationStatus.COMPLETED.name()))
                .andExpect(jsonPath("$[0].statusLabel").value("완료"))
                .andExpect(jsonPath("$[0].createdAt").isString());
    }

    @Test
    void 관리자_리뷰_API는_최신_리뷰부터_조회한다() throws Exception {
        Reservation firstReservation = completedReservation("첫번째고객", "010-1111-1111");
        Reservation secondReservation = completedReservation("두번째고객", "010-2222-2222");
        reviewService.create(reviewCreateRequest(firstReservation.getId(), "010-1111-1111", 4, "첫 번째 리뷰입니다."));
        reviewService.create(reviewCreateRequest(secondReservation.getId(), "010-2222-2222", 5, "두 번째 리뷰입니다."));

        mockMvc.perform(get("/api/admin/reviews")
                .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerName").value("두**"))
                .andExpect(jsonPath("$[1].customerName").value("첫**"));
    }

    @Test
    void 관리자_리뷰_API는_공개상태를_변경한다() throws Exception {
        Reservation reservation = completedReservation("상태변경고객", "010-3333-3333");
        Review review = reviewService.create(reviewCreateRequest(reservation.getId(), "010-3333-3333", 3, "확인이 필요한 리뷰입니다."));

        mockMvc.perform(patch("/api/admin/reviews/{id}/published", review.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "published": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(review.getId()))
                .andExpect(jsonPath("$.reservationId").value(reservation.getId()))
                .andExpect(jsonPath("$.published").value(false));

        mockMvc.perform(patch("/api/admin/reviews/{id}/published", review.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "published": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(review.getId()))
                .andExpect(jsonPath("$.published").value(true));
    }

    @Test
    void 관리자_리뷰_API는_답변을_저장하고_비울_수_있다() throws Exception {
        Reservation reservation = completedReservation("답변고객", "010-4444-4444");
        Review review = reviewService.create(reviewCreateRequest(reservation.getId(), "010-4444-4444", 2, "응대가 아쉬웠습니다."));

        mockMvc.perform(patch("/api/admin/reviews/{id}/reply", review.getId())
                        .with(user("review-admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reply": "불편을 드려 죄송합니다. 담당자가 다시 연락드리겠습니다."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(review.getId()))
                .andExpect(jsonPath("$.reservationId").value(reservation.getId()))
                .andExpect(jsonPath("$.adminReply").value("불편을 드려 죄송합니다. 담당자가 다시 연락드리겠습니다."))
                .andExpect(jsonPath("$.adminRepliedBy").value("review-admin"))
                .andExpect(jsonPath("$.adminRepliedAt").isString());

        mockMvc.perform(patch("/api/admin/reviews/{id}/reply", review.getId())
                        .with(user("review-admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reply": "   "
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(review.getId()))
                .andExpect(jsonPath("$.adminReply").doesNotExist())
                .andExpect(jsonPath("$.adminRepliedBy").doesNotExist())
                .andExpect(jsonPath("$.adminRepliedAt").doesNotExist());
    }

    @Test
    void 관리자_리뷰_API는_너무_긴_답변을_거부한다() throws Exception {
        Reservation reservation = completedReservation("긴답변고객", "010-5555-5555");
        Review review = reviewService.create(reviewCreateRequest(reservation.getId(), "010-5555-5555", 3, "답변 길이 테스트입니다."));

        mockMvc.perform(patch("/api/admin/reviews/{id}/reply", review.getId())
                        .with(user("review-admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reply": "%s"
                                }
                                """.formatted("가".repeat(1001))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("리뷰 답변은 1,000자 이내로 입력해 주세요."));
    }

    private Reservation completedReservation(String customerName, String phone) {
        Reservation reservation = reservationService.create(reservationCreateRequest(customerName, phone));
        reservationService.updateStatus(reservation.getId(), ReservationStatus.CONSULTING, "admin");
        reservationService.updateStatus(reservation.getId(), ReservationStatus.ESTIMATE_SENT, "admin");
        reservationService.updateStatus(reservation.getId(), ReservationStatus.CONFIRMED, "admin");
        reservationService.updateStatus(reservation.getId(), ReservationStatus.COMPLETED, "admin");
        return reservation;
    }

    private ReservationCreateRequest reservationCreateRequest(String customerName, String phone) {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setCustomerName(customerName);
        request.setPhone(phone);
        request.setEmail("admin-review@example.com");
        request.setMoveDate(LocalDate.now().plusDays(7));
        request.setMoveTime(LocalTime.of(10, 30));
        request.setFromAddress("서울시 강남구 테헤란로 1");
        request.setToAddress("서울시 송파구 올림픽로 1");
        request.setMoveType(MoveType.STUDIO);
        request.setFromElevator(true);
        request.setToElevator(true);
        request.setFromFloor(3);
        request.setToFloor(5);
        request.setMemo("관리자 리뷰 API 테스트 예약입니다.");
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
