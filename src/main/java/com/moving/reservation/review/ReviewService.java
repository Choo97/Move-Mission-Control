package com.moving.reservation.review;

import com.moving.reservation.reservation.Reservation;
import com.moving.reservation.reservation.ReservationRepository;
import com.moving.reservation.reservation.ReservationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;

    public ReviewService(ReviewRepository reviewRepository, ReservationRepository reservationRepository) {
        this.reviewRepository = reviewRepository;
        this.reservationRepository = reservationRepository;
    }

    public Optional<Review> findByReservationId(Long reservationId) {
        return reviewRepository.findByReservationId(reservationId);
    }

    public List<Review> findAll() {
        return reviewRepository.findAllWithReservationOrderByCreatedAtDesc();
    }

    public double averageRating() {
        return reviewRepository.averageRating();
    }

    @Transactional
    public Review create(ReviewCreateRequest request) {
        Reservation reservation = reservationRepository.findByIdAndPhone(request.getReservationId(), request.getPhone())
                .orElseThrow(() -> new IllegalArgumentException("예약 번호와 연락처가 일치하지 않습니다."));

        if (reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw new IllegalArgumentException("완료된 예약만 리뷰를 작성할 수 있습니다.");
        }

        if (reviewRepository.existsByReservationId(reservation.getId())) {
            throw new IllegalArgumentException("이미 리뷰가 등록된 예약입니다.");
        }

        return reviewRepository.save(new Review(
                reservation,
                request.getRating(),
                request.getContent().trim()
        ));
    }
}
