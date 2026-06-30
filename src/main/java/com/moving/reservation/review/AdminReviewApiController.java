package com.moving.reservation.review;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewApiController {

    private final ReviewService reviewService;

    public AdminReviewApiController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<AdminReviewResponse> list() {
        return reviewService.findAll().stream()
                .map(AdminReviewResponse::from)
                .toList();
    }
}
