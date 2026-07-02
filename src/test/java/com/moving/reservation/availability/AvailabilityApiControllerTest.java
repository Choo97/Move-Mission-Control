package com.moving.reservation.availability;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class AvailabilityApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 관리자_API로_운영정책을_조회한다() throws Exception {
        mockMvc.perform(get("/api/admin/operating-policy")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minAdvanceDays").isNumber())
                .andExpect(jsonPath("$.maxAdvanceDays").isNumber())
                .andExpect(jsonPath("$.maxDailyReservations").isNumber());
    }

    @Test
    void 관리자_API로_운영정책을_저장한다() throws Exception {
        mockMvc.perform(put("/api/admin/operating-policy")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "minAdvanceDays": 1,
                                  "maxAdvanceDays": 60,
                                  "maxDailyReservations": 6
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minAdvanceDays").value(1))
                .andExpect(jsonPath("$.maxAdvanceDays").value(60))
                .andExpect(jsonPath("$.maxDailyReservations").value(6));
    }

    @Test
    void 운영정책은_시작일이_종료일보다_크면_400을_응답한다() throws Exception {
        mockMvc.perform(put("/api/admin/operating-policy")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "minAdvanceDays": 10,
                                  "maxAdvanceDays": 5,
                                  "maxDailyReservations": 6
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("예약 가능 시작일은 종료일보다 작거나 같아야 합니다."));
    }
}
