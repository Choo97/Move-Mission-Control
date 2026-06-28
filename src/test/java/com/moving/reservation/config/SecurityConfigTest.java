package com.moving.reservation.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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
    void 로그인화면은_React_개발서버로_이동한다() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost:5173/login"));
    }

    @Test
    void React_관리자_로그인_API는_세션을_생성한다() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/admin/session/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "admin1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(get("/api/admin/reservations").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void React_관리자_세션확인_API는_로그인한_관리자정보를_응답한다() throws Exception {
        mockMvc.perform(get("/api/admin/session/me")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    @Test
    void React_관리자_로그아웃_API는_세션을_종료한다() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/admin/session/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "admin1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(get("/api/admin/session/me").session(session))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admin/session/logout").session(session))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("JSESSIONID=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")))
                .andExpect(result -> assertThat(session.isInvalid()).isTrue());
    }

    @Test
    void 로그인하지_않으면_React_관리자_세션확인_API는_401을_응답한다() throws Exception {
        mockMvc.perform(get("/api/admin/session/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void React_관리자_로그인_API는_인증실패시_401을_응답한다() throws Exception {
        mockMvc.perform(post("/api/admin/session/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "admin",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 올바르지 않습니다."));
    }

    @Test
    void 관리자권한이_있으면_관리자_예약목록에_접근할_수_있다() throws Exception {
        mockMvc.perform(get("/admin/reservations")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void 관리자권한이_없으면_관리자_화면에_접근할_수_없다() throws Exception {
        mockMvc.perform(get("/admin/reservations")
                        .with(user("customer").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void 로그인하지_않으면_관리자_계정관리도_로그인화면으로_이동한다() throws Exception {
        mockMvc.perform(get("/admin/account/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void 관리자권한이_없으면_관리자_API에_접근할_수_없다() throws Exception {
        mockMvc.perform(get("/api/admin/reservations")
                        .with(user("customer").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void 로그인하지_않으면_관리자_API는_401을_응답한다() throws Exception {
        mockMvc.perform(get("/api/admin/reservations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 로그인하지_않으면_관리자_API_수정요청도_401을_응답한다() throws Exception {
        mockMvc.perform(patch("/api/admin/reservations/1/status"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 고객용_예약신청화면은_로그인없이_접근할_수_있다() throws Exception {
        mockMvc.perform(get("/reservations/new")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .redirectedUrl("/?view=create"));
    }

    @Test
    void FAQ_API는_로그인없이_접근할_수_있다() throws Exception {
        mockMvc.perform(get("/api/faqs"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].question").isString())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].answer").isString());
    }

    @Test
    void OpenAPI_문서는_로그인없이_접근할_수_있다() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void 공통_오류경로는_로그인_오류로_바뀌지_않는다() throws Exception {
        mockMvc.perform(get("/error"))
                .andExpect(result -> assertThat(result.getResponse().getStatus()).isNotEqualTo(401));
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
    void React_개발서버는_관리자_로그인_API_CORS_사전요청을_보낼_수_있다() throws Exception {
        mockMvc.perform(options("/api/admin/session/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    void React_개발서버는_관리자_로그아웃_API_CORS_사전요청을_보낼_수_있다() throws Exception {
        mockMvc.perform(options("/api/admin/session/logout")
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
