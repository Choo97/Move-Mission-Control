package com.moving.reservation.review;

import com.moving.reservation.api.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
            summary = "공개 리뷰 조회",
            description = "관리자가 공개 처리한 리뷰만 개인정보 없이 최신순으로 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "공개 리뷰 조회 성공")
    @GetMapping("/public")
    public List<PublicReviewResponse> publicReviews(@RequestParam(defaultValue = "6") int limit) {
        return reviewService.findPublished(limit).stream()
                .map(PublicReviewResponse::from)
                .toList();
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
