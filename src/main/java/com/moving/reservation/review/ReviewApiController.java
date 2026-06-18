package com.moving.reservation.review;

import com.moving.reservation.api.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "고객 리뷰 API", description = "완료된 이사 예약에 대해 고객 리뷰를 작성하는 API")
public class ReviewApiController {

    private final ReviewService reviewService;

    public ReviewApiController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(
            summary = "리뷰 작성",
            description = "완료된 예약에 대해 예약 번호와 연락처를 확인한 뒤 고객 리뷰와 평점을 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "리뷰 작성 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류, 연락처 불일치 또는 리뷰 작성 불가 상태")
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ReviewCreateRequest request,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(ApiErrorResponse.badRequest(firstErrorMessage(bindingResult)));
        }

        try {
            Review review = reviewService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ReviewApiResponse.from(review));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiErrorResponse.badRequest(exception.getMessage()));
        }
    }

    private String firstErrorMessage(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("요청 정보를 확인해 주세요.");
    }
}
