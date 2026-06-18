package com.moving.reservation.review;

import com.moving.reservation.api.ApiErrorResponse;
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
public class ReviewApiController {

    private final ReviewService reviewService;

    public ReviewApiController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

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
