package com.moving.reservation.reservation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:reservation-api-schedule-conflict-response-test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "reservation.schedule-conflict.enabled=true"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservationApiScheduleConflictResponseTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 예약신청_API는_선택할수없는_시간이면_400과_안내문구를_응답한다() throws Exception {
        LocalDate moveDate = nextOpenDate();

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "시간오류고객",
                                  "phone": "010-1212-3434",
                                  "email": "schedule-conflict@example.com",
                                  "moveDate": "%s",
                                  "moveTime": "09:10",
                                  "fromAddress": "서울시 강남구 테헤란로 1",
                                  "toAddress": "서울시 송파구 올림픽로 1",
                                  "moveType": "STUDIO",
                                  "fromElevator": true,
                                  "toElevator": true,
                                  "fromFloor": 3,
                                  "toFloor": 5,
                                  "fromLadderTruck": false,
                                  "toLadderTruck": false,
                                  "memo": "운영 시간 오류 응답 테스트입니다."
                                }
                                """.formatted(moveDate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("선택한 날짜와 시간에는 예약할 수 없습니다. 다른 시간을 선택해 주세요."));
    }

    private LocalDate nextOpenDate() {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
