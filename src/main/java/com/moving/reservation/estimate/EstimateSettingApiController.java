package com.moving.reservation.estimate;

import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/estimate-settings")
public class EstimateSettingApiController {

    private final EstimateSettingService estimateSettingService;

    public EstimateSettingApiController(EstimateSettingService estimateSettingService) {
        this.estimateSettingService = estimateSettingService;
    }

    @GetMapping
    public EstimateSettingPageResponse list() {
        return response();
    }

    @PatchMapping("/{id}")
    public EstimateSettingPageResponse update(@PathVariable Long id,
                                              @RequestBody EstimateSettingUpdateRequest request,
                                              Principal principal) {
        if (request == null || request.amount() == null) {
            throw new IllegalArgumentException("견적 기준 값을 입력해 주세요.");
        }

        String changedBy = principal == null ? "system" : principal.getName();
        estimateSettingService.update(id, request.amount(), changedBy);
        return response();
    }

    private EstimateSettingPageResponse response() {
        return new EstimateSettingPageResponse(
                estimateSettingService.findAll().stream()
                        .map(EstimateSettingResponse::from)
                        .toList(),
                estimateSettingService.findHistories().stream()
                        .map(EstimateSettingHistoryResponse::from)
                        .toList()
        );
    }
}
