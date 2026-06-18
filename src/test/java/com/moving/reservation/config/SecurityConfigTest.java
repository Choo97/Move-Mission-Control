package com.moving.reservation.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 로그인하지_않으면_관리자_예약목록은_로그인화면으로_이동한다() throws Exception {
        mockMvc.perform(get("/admin/reservations"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void 관리자_로그인에_성공하면_예약목록으로_이동한다() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .user("admin")
                        .password("admin1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reservations"));
    }

    @Test
    void 관리자_로그인에_실패하면_로그인_오류화면으로_이동한다() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .user("admin")
                        .password("wrong-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void 관리자권한이_있으면_관리자_예약목록에_접근할_수_있다() throws Exception {
        mockMvc.perform(get("/admin/reservations")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void 고객용_예약신청화면은_로그인없이_접근할_수_있다() throws Exception {
        mockMvc.perform(get("/reservations/new")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void OpenAPI_문서는_로그인없이_접근할_수_있다() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void 업로드파일은_로그인없이_요청할_수_있다() throws Exception {
        mockMvc.perform(get("/uploads/reservation-photos/not-found.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    void React_개발서버는_API_CORS_사전요청을_보낼_수_있다() throws Exception {
        mockMvc.perform(options("/api/reservations")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    void React_개발서버는_업로드파일_CORS_응답을_받을_수_있다() throws Exception {
        mockMvc.perform(get("/uploads/reservation-photos/not-found.jpg")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000"))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"));
    }
}
