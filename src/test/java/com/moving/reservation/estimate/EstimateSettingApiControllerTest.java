package com.moving.reservation.estimate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class EstimateSettingApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EstimateSettingRepository estimateSettingRepository;

    @Autowired
    private EstimateSettingHistoryRepository estimateSettingHistoryRepository;

    @Test
    void 관리자_API로_견적정책을_조회한다() throws Exception {
        mockMvc.perform(get("/api/admin/estimate-settings")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.settings").isArray())
                .andExpect(jsonPath("$.settings[0].id").isNumber())
                .andExpect(jsonPath("$.settings[0].settingKey").isString())
                .andExpect(jsonPath("$.settings[0].label").isString())
                .andExpect(jsonPath("$.settings[0].amount").isNumber())
                .andExpect(jsonPath("$.settings[0].unit").isString())
                .andExpect(jsonPath("$.histories").isArray());
    }

    @Test
    void 관리자_API로_견적정책을_수정하면_변경이력을_남긴다() throws Exception {
        EstimateSetting setting = estimateSettingRepository.findBySettingKey(EstimateSettingKey.STUDIO_BASE)
                .orElseThrow();

        mockMvc.perform(patch("/api/admin/estimate-settings/{id}", setting.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 190000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.histories[0].settingKey").value("STUDIO_BASE"))
                .andExpect(jsonPath("$.histories[0].previousAmount").value(180000))
                .andExpect(jsonPath("$.histories[0].changedAmount").value(190000))
                .andExpect(jsonPath("$.histories[0].changedBy").value("admin"));

        EstimateSetting updated = estimateSettingRepository.findBySettingKey(EstimateSettingKey.STUDIO_BASE)
                .orElseThrow();
        assertThat(updated.getAmount()).isEqualTo(190000);
        assertThat(estimateSettingHistoryRepository.findAllByOrderByChangedAtDesc())
                .anySatisfy(history -> {
                    assertThat(history.getSettingKey()).isEqualTo(EstimateSettingKey.STUDIO_BASE);
                    assertThat(history.getPreviousAmount()).isEqualTo(180000);
                    assertThat(history.getChangedAmount()).isEqualTo(190000);
                    assertThat(history.getChangedBy()).isEqualTo("admin");
                });
    }

    @Test
    void 견적정책은_음수로_수정할수없다() throws Exception {
        EstimateSetting setting = estimateSettingRepository.findBySettingKey(EstimateSettingKey.STUDIO_BASE)
                .orElseThrow();

        mockMvc.perform(patch("/api/admin/estimate-settings/{id}", setting.getId())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("견적 기준 값은 0 이상이어야 합니다."));
    }
}
