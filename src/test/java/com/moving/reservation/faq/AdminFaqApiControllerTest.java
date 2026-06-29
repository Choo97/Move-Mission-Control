package com.moving.reservation.faq;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class AdminFaqApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 관리자_FAQ_API는_로그인이_필요하다() throws Exception {
        mockMvc.perform(get("/api/admin/faqs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 관리자_FAQ_API는_전체_FAQ를_조회한다() throws Exception {
        mockMvc.perform(get("/api/admin/faqs")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].question").value("예약 후에는 어떻게 확인하나요?"))
                .andExpect(jsonPath("$[0].displayOrder").value(1))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void 관리자_FAQ_API는_FAQ를_추가_수정_숨김처리한다() throws Exception {
        String createResponse = mockMvc.perform(post("/api/admin/faqs")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "question": "React FAQ는 어디서 관리하나요?",
                                  "answer": "관리자 FAQ 화면에서 등록하고 공개 여부를 관리합니다.",
                                  "displayOrder": 20
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.question").value("React FAQ는 어디서 관리하나요?"))
                .andExpect(jsonPath("$.answer").value("관리자 FAQ 화면에서 등록하고 공개 여부를 관리합니다."))
                .andExpect(jsonPath("$.displayOrder").value(20))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number faqId = com.jayway.jsonpath.JsonPath.read(createResponse, "$.id");

        mockMvc.perform(patch("/api/admin/faqs/{id}", faqId.longValue())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "question": "수정된 FAQ 질문",
                                  "answer": "수정된 답변입니다.",
                                  "displayOrder": 3
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("수정된 FAQ 질문"))
                .andExpect(jsonPath("$.answer").value("수정된 답변입니다."))
                .andExpect(jsonPath("$.displayOrder").value(3));

        mockMvc.perform(patch("/api/admin/faqs/{id}/active", faqId.longValue())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "active": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void 관리자_FAQ_API는_잘못된_입력이면_400을_응답한다() throws Exception {
        mockMvc.perform(post("/api/admin/faqs")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "question": "",
                                  "answer": "답변입니다.",
                                  "displayOrder": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("질문을 입력해 주세요."));
    }
}
