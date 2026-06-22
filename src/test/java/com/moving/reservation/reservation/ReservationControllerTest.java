package com.moving.reservation.reservation;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationService reservationService;

    @Test
    void 예약신청_화면을_로그인없이_조회한다() throws Exception {
        mockMvc.perform(get("/reservations/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?view=create"));
    }

    @Test
    void 예약조회_화면을_로그인없이_조회한다() throws Exception {
        mockMvc.perform(get("/reservations/search"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?view=search"));
    }

    @Test
    void 인증없이_예약상세에_접근하면_예약조회_화면으로_이동한다() throws Exception {
        Reservation reservation = reservationService.create(reservationCreateRequest("010-1234-5678"));

        mockMvc.perform(get("/reservations/{id}", reservation.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?view=search&reservationId=" + reservation.getId()));
    }

    @Test
    void 예약번호와_연락처로_조회하면_상세화면_접근권한을_얻는다() throws Exception {
        Reservation reservation = reservationService.create(reservationCreateRequest("010-1234-5678"));

        MvcResult searchResult = mockMvc.perform(post("/reservations/search")
                        .with(csrf())
                        .param("reservationId", reservation.getId().toString())
                        .param("phone", "010-1234-5678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations/" + reservation.getId()))
                .andReturn();

        MockHttpSession session = (MockHttpSession) searchResult.getRequest().getSession(false);

        mockMvc.perform(get("/reservations/{id}", reservation.getId())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?view=search&reservationId=" + reservation.getId()));
    }

    @Test
    void 연락처가_다르면_예약조회_화면에_오류를_보여준다() throws Exception {
        Reservation reservation = reservationService.create(reservationCreateRequest("010-1234-5678"));

        mockMvc.perform(post("/reservations/search")
                        .with(csrf())
                        .param("reservationId", reservation.getId().toString())
                        .param("phone", "010-0000-0000"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation/search"))
                .andExpect(model().attribute("searchError", "예약 번호와 연락처가 일치하는 예약을 찾을 수 없습니다."));
    }

    @Test
    void 예약신청을_완료하면_상세화면으로_이동한다() throws Exception {
        mockMvc.perform(post("/reservations")
                        .param("customerName", "고객화면고객")
                        .param("phone", "010-1234-5678")
                        .param("email", "customer-view@example.com")
                        .param("moveDate", LocalDate.now().plusDays(7).toString())
                        .param("moveTime", LocalTime.of(10, 30).toString())
                        .param("fromAddress", "서울시 강남구 테헤란로 1")
                        .param("toAddress", "서울시 송파구 올림픽로 1")
                        .param("moveType", MoveType.STUDIO.name())
                        .param("fromElevator", "true")
                        .param("toElevator", "true")
                        .param("fromFloor", "3")
                        .param("toFloor", "5")
                        .param("memo", "고객 화면 테스트 예약입니다."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/reservations/*"));
    }

    private ReservationCreateRequest reservationCreateRequest(String phone) {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setCustomerName("고객화면고객");
        request.setPhone(phone);
        request.setEmail("customer-view@example.com");
        request.setMoveDate(LocalDate.now().plusDays(7));
        request.setMoveTime(LocalTime.of(10, 30));
        request.setFromAddress("서울시 강남구 테헤란로 1");
        request.setToAddress("서울시 송파구 올림픽로 1");
        request.setMoveType(MoveType.STUDIO);
        request.setFromElevator(true);
        request.setToElevator(true);
        request.setFromFloor(3);
        request.setToFloor(5);
        request.setMemo("고객 화면 테스트 예약입니다.");
        return request;
    }
}
