package com.moving.reservation.admin;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminReservationApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationService reservationService;

    @Test
    void 관리자_예약목록_API는_로그인이_필요하다() throws Exception {
        mockMvc.perform(get("/api/admin/reservations"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void 관리자_예약목록_API는_예약목록을_JSON으로_조회한다() throws Exception {
        reservationService.create(reservationCreateRequest("React관리자고객", "010-1111-2222"));

        mockMvc.perform(get("/api/admin/reservations")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").isNumber())
                .andExpect(jsonPath("$.content[0].customerName").value("React관리자고객"))
                .andExpect(jsonPath("$.content[0].phone").value("010-1111-2222"))
                .andExpect(jsonPath("$.content[0].status").value(ReservationStatus.RECEIVED.name()))
                .andExpect(jsonPath("$.content[0].statusLabel").value("접수"))
                .andExpect(jsonPath("$.content[0].moveType").value(MoveType.STUDIO.name()))
                .andExpect(jsonPath("$.content[0].finalEstimatedPrice").isNumber())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void 관리자_예약목록_API는_상태와_페이지_크기를_적용한다() throws Exception {
        reservationService.create(reservationCreateRequest("조회대상", "010-1111-2222"));
        reservationService.create(reservationCreateRequest("조회제외", "010-3333-4444"));

        mockMvc.perform(get("/api/admin/reservations")
                        .param("status", ReservationStatus.RECEIVED.name())
                        .param("size", "20")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.pageSize").value(20))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void 관리자_예약상세_API는_예약상세를_JSON으로_조회한다() throws Exception {
        var reservation = reservationService.create(reservationCreateRequest("상세조회고객", "010-5555-6666"));
        reservationService.updateStatus(reservation.getId(), ReservationStatus.CONSULTING, "admin");
        reservationService.updateAdminMemo(reservation.getId(), "관리자 확인 메모", "admin");

        mockMvc.perform(get("/api/admin/reservations/{id}", reservation.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservation.getId()))
                .andExpect(jsonPath("$.customerName").value("상세조회고객"))
                .andExpect(jsonPath("$.phone").value("010-5555-6666"))
                .andExpect(jsonPath("$.email").value("admin-api@example.com"))
                .andExpect(jsonPath("$.status").value(ReservationStatus.CONSULTING.name()))
                .andExpect(jsonPath("$.statusLabel").value("상담중"))
                .andExpect(jsonPath("$.memo").value("관리자 API 테스트 예약입니다."))
                .andExpect(jsonPath("$.adminMemo").value("관리자 확인 메모"))
                .andExpect(jsonPath("$.estimateLines").isArray())
                .andExpect(jsonPath("$.photos").isArray())
                .andExpect(jsonPath("$.statusHistories[0].changedStatus").value(ReservationStatus.CONSULTING.name()))
                .andExpect(jsonPath("$.customerActionHistories").isArray());
    }

    @Test
    void 관리자_예약상세_API도_로그인이_필요하다() throws Exception {
        var reservation = reservationService.create(reservationCreateRequest("상세권한고객", "010-7777-8888"));

        mockMvc.perform(get("/api/admin/reservations/{id}", reservation.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void 관리자_예약상태_API는_상태를_변경하고_상세를_JSON으로_응답한다() throws Exception {
        var reservation = reservationService.create(reservationCreateRequest("상태변경고객", "010-1111-9999"));

        mockMvc.perform(patch("/api/admin/reservations/{id}/status", reservation.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CONSULTING"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservation.getId()))
                .andExpect(jsonPath("$.status").value(ReservationStatus.CONSULTING.name()))
                .andExpect(jsonPath("$.statusLabel").value("상담중"))
                .andExpect(jsonPath("$.statusHistories[0].changedStatus").value(ReservationStatus.CONSULTING.name()))
                .andExpect(jsonPath("$.statusHistories[0].changedBy").value("admin"));
    }

    @Test
    void 관리자_예약상태_API는_불가능한_상태전환이면_400을_응답한다() throws Exception {
        var reservation = reservationService.create(reservationCreateRequest("상태오류고객", "010-2222-9999"));

        mockMvc.perform(patch("/api/admin/reservations/{id}/status", reservation.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "COMPLETED"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("현재 상태에서는 '완료'(으)로 변경할 수 없습니다."));
    }

    @Test
    void 관리자_예약상태_API는_CSRF_토큰없이_호출할_수_있다() throws Exception {
        var reservation = reservationService.create(reservationCreateRequest("상태CSRF고객", "010-3333-9999"));

        mockMvc.perform(patch("/api/admin/reservations/{id}/status", reservation.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "CONSULTING"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void 관리자_견적금액_API는_견적을_저장하고_상세를_JSON으로_응답한다() throws Exception {
        var reservation = reservationService.create(reservationCreateRequest("견적변경고객", "010-4444-9999"));
        reservationService.updateStatus(reservation.getId(), ReservationStatus.CONSULTING, "admin");

        mockMvc.perform(patch("/api/admin/reservations/{id}/estimate", reservation.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estimatedPrice": 350000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservation.getId()))
                .andExpect(jsonPath("$.estimatedPrice").value(350000))
                .andExpect(jsonPath("$.finalEstimatedPrice").value(350000))
                .andExpect(jsonPath("$.status").value(ReservationStatus.ESTIMATE_SENT.name()))
                .andExpect(jsonPath("$.statusLabel").value("견적안내"));
    }

    @Test
    void 관리자_견적금액_API는_음수면_400을_응답한다() throws Exception {
        var reservation = reservationService.create(reservationCreateRequest("견적오류고객", "010-5555-9999"));

        mockMvc.perform(patch("/api/admin/reservations/{id}/estimate", reservation.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estimatedPrice": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("견적 금액은 0원 이상이어야 합니다."));
    }

    @Test
    void 관리자_견적금액_API는_CSRF_토큰없이_호출할_수_있다() throws Exception {
        var reservation = reservationService.create(reservationCreateRequest("견적CSRF고객", "010-6666-9999"));

        mockMvc.perform(patch("/api/admin/reservations/{id}/estimate", reservation.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "estimatedPrice": 320000
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void 관리자_이동거리_API는_거리를_저장하고_상세를_JSON으로_응답한다() throws Exception {
        var reservation = reservationService.create(reservationCreateRequest("거리변경고객", "010-7777-9999"));

        mockMvc.perform(patch("/api/admin/reservations/{id}/distance", reservation.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "distanceKm": 12
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservation.getId()))
                .andExpect(jsonPath("$.distanceKm").value(12))
                .andExpect(jsonPath("$.estimatedPrice").isNumber())
                .andExpect(jsonPath("$.finalEstimatedPrice").isNumber());
    }

    @Test
    void 관리자_이동거리_API는_null이면_확인전으로_저장한다() throws Exception {
        var reservation = reservationService.create(reservationCreateRequest("거리초기화고객", "010-8888-9999"));
        reservationService.updateDistance(reservation.getId(), 12);

        mockMvc.perform(patch("/api/admin/reservations/{id}/distance", reservation.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "distanceKm": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservation.getId()))
                .andExpect(jsonPath("$.distanceKm").doesNotExist());
    }

    @Test
    void 관리자_이동거리_API는_음수면_400을_응답한다() throws Exception {
        var reservation = reservationService.create(reservationCreateRequest("거리오류고객", "010-9999-0001"));

        mockMvc.perform(patch("/api/admin/reservations/{id}/distance", reservation.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "distanceKm": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("이동 거리는 0km 이상이어야 합니다."));
    }

    @Test
    void 관리자_이동거리_API는_CSRF_토큰없이_호출할_수_있다() throws Exception {
        var reservation = reservationService.create(reservationCreateRequest("거리CSRF고객", "010-9999-0002"));

        mockMvc.perform(patch("/api/admin/reservations/{id}/distance", reservation.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "distanceKm": 7
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void 관리자_이동거리_자동계산_API는_Kakao키가_없으면_400을_응답한다() throws Exception {
        var reservation = reservationService.create(reservationCreateRequest("거리자동고객", "010-9999-0003"));

        mockMvc.perform(patch("/api/admin/reservations/{id}/distance/calculate", reservation.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Kakao REST API 키를 설정하면 자동 거리 계산을 사용할 수 있습니다."));
    }

    private ReservationCreateRequest reservationCreateRequest(String customerName, String phone) {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setCustomerName(customerName);
        request.setPhone(phone);
        request.setEmail("admin-api@example.com");
        request.setMoveDate(LocalDate.now().plusDays(7));
        request.setMoveTime(LocalTime.of(10, 30));
        request.setFromAddress("서울시 강남구 테헤란로 1");
        request.setToAddress("서울시 송파구 올림픽로 1");
        request.setMoveType(MoveType.STUDIO);
        request.setFromElevator(true);
        request.setToElevator(true);
        request.setFromFloor(3);
        request.setToFloor(5);
        request.setMemo("관리자 API 테스트 예약입니다.");
        return request;
    }
}
