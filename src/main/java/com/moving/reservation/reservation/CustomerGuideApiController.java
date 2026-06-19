package com.moving.reservation.reservation;

import com.moving.reservation.api.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer-guides")
@Tag(name = "고객 안내 API", description = "예약 상태별 고객 안내 문구 조회 API")
public class CustomerGuideApiController {

    private final CustomerGuideService customerGuideService;

    public CustomerGuideApiController(CustomerGuideService customerGuideService) {
        this.customerGuideService = customerGuideService;
    }

    @Operation(
            summary = "상태별 고객 안내 조회",
            description = "예약 상태에 맞는 공개 고객 안내 문구를 정렬 순서대로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "고객 안내 조회 성공"),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 예약 상태")
    })
    @GetMapping("/{status}")
    public ResponseEntity<?> findByStatus(
            @Parameter(description = "예약 상태", example = "RECEIVED") @PathVariable String status) {
        try {
            ReservationStatus reservationStatus = ReservationStatus.valueOf(status);
            List<CustomerGuideItem> guides = customerGuideService.findActiveItems(reservationStatus);
            return ResponseEntity.ok(guides);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiErrorResponse.badRequest("존재하지 않는 예약 상태입니다."));
        }
    }
}
