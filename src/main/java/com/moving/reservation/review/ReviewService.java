package com.moving.reservation.review;

import com.moving.reservation.privacy.PrivacyHashService;
import com.moving.reservation.reservation.Reservation;
import com.moving.reservation.reservation.ReservationRepository;
import com.moving.reservation.reservation.ReservationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final PrivacyHashService privacyHashService;

    public ReviewService(ReviewRepository reviewRepository,
                         ReservationRepository reservationRepository,
                         PrivacyHashService privacyHashService) {
        this.reviewRepository = reviewRepository;
        this.reservationRepository = reservationRepository;
        this.privacyHashService = privacyHashService;
    }

    public Optional<Review> findByReservationId(Long reservationId) {
        return reviewRepository.findByReservationId(reservationId);
    }

    public List<Review> findAll() {
        return reviewRepository.findAllWithReservationOrderByCreatedAtDesc();
    }

    public List<Review> findRecent() {
        return findAll().stream()
                .limit(3)
                .toList();
    }

    public List<Review> findPublished(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 12));
        return reviewRepository.findPublishedWithReservationOrderByCreatedAtDesc(PageRequest.of(0, safeLimit));
    }

    public double averageRating() {
        return reviewRepository.averageRating();
    }

    @Transactional
    public Review publish(Long id) {
        Review review = get(id);
        review.publish();
        return review;
    }

    @Transactional
    public Review hide(Long id) {
        Review review = get(id);
        review.hide();
        return review;
    }

    @Transactional
    public Review updateAdminReply(Long id, String reply, String adminUsername) {
        Review review = get(id);
        String normalizedReply = normalizeReply(reply);
        if (!StringUtils.hasText(normalizedReply)) {
            review.clearAdminReply();
            return review;
        }

        review.updateAdminReply(normalizedReply, normalizeAdminUsername(adminUsername));
        return review;
    }

    @Transactional
    public Review create(ReviewCreateRequest request) {
        Reservation reservation = findReservationByCustomerPhone(request.getReservationId(), request.getPhone());

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

    private Review get(Long id) {
        return reviewRepository.findByIdWithReservation(id)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
    }

    private Reservation findReservationByCustomerPhone(Long reservationId, String phone) {
        String phoneHash = privacyHashService.phoneHash(phone);

        if (phoneHash != null) {
            Optional<Reservation> reservation = reservationRepository.findByIdAndPhoneHash(reservationId, phoneHash);

            if (reservation.isPresent()) {
                return reservation.get();
            }
        }

        return reservationRepository.findById(reservationId)
                .filter(reservation -> privacyHashService.matchesPhone(reservation.getPhone(), phone))
                .orElseThrow(() -> new IllegalArgumentException("예약 번호와 연락처가 일치하지 않습니다."));
    }

    private String normalizeReply(String reply) {
        if (!StringUtils.hasText(reply)) {
            return "";
        }

        String trimmedReply = reply.trim();
        if (trimmedReply.length() > 1000) {
            throw new IllegalArgumentException("리뷰 답변은 1,000자 이내로 입력해 주세요.");
        }

        return trimmedReply;
    }

    private String normalizeAdminUsername(String adminUsername) {
        if (!StringUtils.hasText(adminUsername)) {
            return "admin";
        }

        String trimmedUsername = adminUsername.trim();
        return trimmedUsername.length() > 100
                ? trimmedUsername.substring(0, 100)
                : trimmedUsername;
    }
}
