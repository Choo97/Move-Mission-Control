package com.moving.reservation.reservation;

import com.moving.reservation.coupon.Coupon;
import com.moving.reservation.coupon.CouponService;
import com.moving.reservation.map.KakaoDistanceService;
import com.moving.reservation.notification.CustomerNotificationService;
import com.moving.reservation.review.ReviewService;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationStatusHistoryRepository statusHistoryRepository;
    private final ReservationPhotoRepository reservationPhotoRepository;
    private final ReservationPhotoStorage reservationPhotoStorage;
    private final CouponService couponService;
    private final ReviewService reviewService;
    private final ReservationEstimateCalculator estimateCalculator;
    private final KakaoDistanceService kakaoDistanceService;
    private final CustomerNotificationService customerNotificationService;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationStatusHistoryRepository statusHistoryRepository,
                              ReservationPhotoRepository reservationPhotoRepository,
                              ReservationPhotoStorage reservationPhotoStorage,
                              CouponService couponService,
                              ReviewService reviewService,
                              ReservationEstimateCalculator estimateCalculator,
                              KakaoDistanceService kakaoDistanceService,
                              CustomerNotificationService customerNotificationService) {
        this.reservationRepository = reservationRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.reservationPhotoRepository = reservationPhotoRepository;
        this.reservationPhotoStorage = reservationPhotoStorage;
        this.couponService = couponService;
        this.reviewService = reviewService;
        this.estimateCalculator = estimateCalculator;
        this.kakaoDistanceService = kakaoDistanceService;
        this.customerNotificationService = customerNotificationService;
    }

    @Transactional
    public Reservation create(ReservationCreateRequest request) {
        Reservation reservation = request.toEntity();
        reservation.applyBaseEstimate(estimateCalculator.calculate(
                request.getMoveType(),
                request.isFromElevator(),
                request.isToElevator(),
                request.getFromFloor(),
                request.getToFloor(),
                request.isFromLadderTruck(),
                request.isToLadderTruck(),
                null
        ));

        Coupon coupon = couponService.findActiveByCode(request.getCouponCode());

        if (coupon != null) {
            reservation.applyCoupon(coupon);
        }

        reservationRepository.save(reservation);
        customerNotificationService.prepareReservationCreated(reservation);

        request.getItemPhotos().stream()
                .filter(itemPhoto -> itemPhoto != null && !itemPhoto.isEmpty())
                .map(reservationPhotoStorage::store)
                .map(storedPhoto -> new ReservationPhoto(
                        reservation,
                        storedPhoto.originalFilename(),
                        storedPhoto.storedFilename(),
                        storedPhoto.fileUrl()
                ))
                .forEach(reservationPhotoRepository::save);

        return reservation;
    }

    public Reservation get(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
    }

    public Reservation search(ReservationSearchRequest request) {
        return reservationRepository.findByIdAndPhone(request.getReservationId(), request.getPhone())
                .orElseThrow(() -> new IllegalArgumentException("예약 번호와 연락처가 일치하는 예약을 찾을 수 없습니다."));
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAllByOrderByMoveDateAscMoveTimeAsc();
    }

    public List<Reservation> findRecent() {
        return reservationRepository.findTop5ByOrderByCreatedAtDesc();
    }

    public List<Reservation> search(ReservationStatus status, String keyword, LocalDate startDate, LocalDate endDate) {
        return search(status, keyword, startDate, endDate, false, ReservationSort.PRIORITY);
    }

    public List<Reservation> search(ReservationStatus status,
                                    String keyword,
                                    LocalDate startDate,
                                    LocalDate endDate,
                                    Boolean needsDistance) {
        return search(status, keyword, startDate, endDate, needsDistance, ReservationSort.PRIORITY);
    }

    public List<Reservation> search(ReservationStatus status,
                                    String keyword,
                                    LocalDate startDate,
                                    LocalDate endDate,
                                    Boolean needsDistance,
                                    ReservationSort sort) {
        return reservationRepository.search(status, normalizeKeyword(keyword), startDate, endDate, needsDistance).stream()
                .sorted(comparator(sort))
                .toList();
    }

    public Page<Reservation> searchPage(ReservationStatus status,
                                        String keyword,
                                        LocalDate startDate,
                                        LocalDate endDate,
                                        Boolean needsDistance,
                                        ReservationSort sort,
                                        Pageable pageable) {
        List<Reservation> reservations = search(status, keyword, startDate, endDate, needsDistance, sort);
        int pageSize = pageable.getPageSize();
        int pageNumber = Math.max(pageable.getPageNumber(), 0);
        int start = Math.min(pageNumber * pageSize, reservations.size());
        int end = Math.min(start + pageSize, reservations.size());

        return new PageImpl<>(
                reservations.subList(start, end),
                PageRequest.of(pageNumber, pageSize),
                reservations.size()
        );
    }

    public List<ReservationStatusHistory> findStatusHistories(Long reservationId) {
        return statusHistoryRepository.findByReservationIdOrderByChangedAtDesc(reservationId);
    }

    public List<ReservationPhoto> findPhotos(Long reservationId) {
        return reservationPhotoRepository.findByReservationIdOrderByUploadedAtAsc(reservationId);
    }

    public List<ReservationEstimateLine> estimateLines(Reservation reservation) {
        return estimateCalculator.calculateLines(
                reservation.getMoveType(),
                reservation.isFromElevator(),
                reservation.isToElevator(),
                reservation.getFromFloor(),
                reservation.getToFloor(),
                reservation.isFromLadderTruck(),
                reservation.isToLadderTruck(),
                reservation.getDistanceKm()
        );
    }

    public ReservationEstimatePreviewResponse previewEstimate(MoveType moveType,
                                                              boolean fromElevator,
                                                              boolean toElevator,
                                                              Integer fromFloor,
                                                              Integer toFloor,
                                                              boolean fromLadderTruck,
                                                              boolean toLadderTruck,
                                                              Integer distanceKm,
                                                              String couponCode) {
        List<ReservationEstimateLine> estimateLines = estimateCalculator.calculateLines(
                moveType,
                fromElevator,
                toElevator,
                fromFloor,
                toFloor,
                fromLadderTruck,
                toLadderTruck,
                distanceKm
        );
        int estimatedPrice = estimateLines.stream()
                .mapToInt(ReservationEstimateLine::amount)
                .sum();
        Coupon coupon = couponService.findActiveByCode(couponCode);
        int discountAmount = coupon == null ? 0 : coupon.calculateDiscount(estimatedPrice);

        return new ReservationEstimatePreviewResponse(
                estimateLines.stream()
                        .map(line -> new ReservationEstimatePreviewLine(line.label(), line.amount()))
                        .toList(),
                estimatedPrice,
                discountAmount,
                Math.max(0, estimatedPrice - discountAmount),
                coupon == null ? null : coupon.getName()
        );
    }

    public List<Reservation> findCouponUsages() {
        return reservationRepository.findCouponUsages();
    }

    @Transactional
    public void updateStatus(Long id, ReservationStatus status, String changedBy) {
        Reservation reservation = get(id);
        changeStatus(reservation, status, changedBy);
    }

    @Transactional
    public void cancel(Long id, String phone) {
        Reservation reservation = reservationRepository.findByIdAndPhone(id, phone)
                .orElseThrow(() -> new IllegalArgumentException("예약 번호와 연락처가 일치하지 않습니다."));

        if (!reservation.isCancelable()) {
            throw new IllegalArgumentException("현재 상태에서는 예약을 취소할 수 없습니다.");
        }

        changeStatus(reservation, ReservationStatus.CANCELED, "customer");
    }

    @Transactional
    public void acceptEstimate(Long id) {
        Reservation reservation = get(id);

        if (!reservation.isEstimateAcceptable()) {
            throw new IllegalArgumentException("현재 상태에서는 견적을 동의할 수 없습니다.");
        }

        if (reservation.getStatus() != ReservationStatus.ESTIMATE_SENT
                && reservation.getStatus().canTransitionTo(ReservationStatus.ESTIMATE_SENT)) {
            changeStatus(reservation, ReservationStatus.ESTIMATE_SENT, "customer");
        }

        reservation.acceptEstimate();
        changeStatus(reservation, ReservationStatus.CONFIRMED, "customer");
    }

    @Transactional
    public void updateDetails(Long id, ReservationUpdateRequest request) {
        Reservation reservation = reservationRepository.findByIdAndPhone(id, request.getPhone())
                .orElseThrow(() -> new IllegalArgumentException("예약 번호와 연락처가 일치하지 않습니다."));

        if (!reservation.isEditable()) {
            throw new IllegalArgumentException("현재 상태에서는 예약을 수정할 수 없습니다.");
        }

        reservation.updateDetails(
                request.getMoveDate(),
                request.getMoveTime(),
                request.getFromAddress(),
                request.getToAddress(),
                request.getFromFloor(),
                request.getToFloor(),
                request.isFromLadderTruck(),
                request.isToLadderTruck(),
                request.getEmail(),
                request.getMemo()
        );
        reservation.applyBaseEstimate(estimateCalculator.calculate(
                reservation.getMoveType(),
                reservation.isFromElevator(),
                reservation.isToElevator(),
                reservation.getFromFloor(),
                reservation.getToFloor(),
                reservation.isFromLadderTruck(),
                reservation.isToLadderTruck(),
                reservation.getDistanceKm()
        ));
    }

    @Transactional
    public void updateDistance(Long id, Integer distanceKm) {
        Reservation reservation = get(id);
        reservation.updateDistance(distanceKm);
        recalculateBaseEstimate(reservation);
    }

    @Transactional
    public int calculateAndUpdateDistance(Long id) {
        Reservation reservation = get(id);
        int distanceKm = kakaoDistanceService.calculateDistanceKm(
                reservation.getFromAddress(),
                reservation.getToAddress()
        );
        reservation.updateDistance(distanceKm);
        recalculateBaseEstimate(reservation);
        return distanceKm;
    }

    private void changeStatus(Reservation reservation, ReservationStatus status, String changedBy) {
        ReservationStatus previousStatus = reservation.getStatus();

        if (previousStatus == status) {
            return;
        }

        if (!previousStatus.canTransitionTo(status)) {
            throw new IllegalArgumentException("현재 상태에서는 '" + status.getLabel() + "'(으)로 변경할 수 없습니다.");
        }

        reservation.updateStatus(status);
        statusHistoryRepository.save(new ReservationStatusHistory(reservation, previousStatus, status, changedBy));
        customerNotificationService.prepareStatusChanged(reservation, status);
    }

    private void recalculateBaseEstimate(Reservation reservation) {
        reservation.applyBaseEstimate(estimateCalculator.calculate(
                reservation.getMoveType(),
                reservation.isFromElevator(),
                reservation.isToElevator(),
                reservation.getFromFloor(),
                reservation.getToFloor(),
                reservation.isFromLadderTruck(),
                reservation.isToLadderTruck(),
                reservation.getDistanceKm()
        ));
    }

    @Transactional
    public void updateEstimate(Long id, Integer estimatedPrice) {
        Reservation reservation = get(id);
        reservation.updateEstimate(estimatedPrice);
        if (reservation.getStatus().canTransitionTo(ReservationStatus.ESTIMATE_SENT)) {
            changeStatus(reservation, ReservationStatus.ESTIMATE_SENT, "admin");
        }
        customerNotificationService.prepareEstimateUpdated(reservation);
    }

    @Transactional
    public void updateAdminMemo(Long id, String adminMemo, String updatedBy) {
        get(id).updateAdminMemo(adminMemo, updatedBy);
    }

    public ReservationSummary summary() {
        LocalDate currentDate = LocalDate.now();

        return new ReservationSummary(
                reservationRepository.count(),
                reservationRepository.countByStatus(ReservationStatus.RECEIVED),
                reservationRepository.countByStatus(ReservationStatus.CONSULTING),
                reservationRepository.countByStatus(ReservationStatus.ESTIMATE_SENT),
                reservationRepository.countByStatus(ReservationStatus.CONFIRMED),
                reservationRepository.countByStatus(ReservationStatus.COMPLETED),
                reservationRepository.countByMoveDate(currentDate),
                reservationRepository.countCouponUsages(),
                reviewService.averageRating()
        );
    }

    public long countEstimateAcceptancePending() {
        return reservationRepository.countByStatusAndEstimateAcceptedAtIsNull(ReservationStatus.ESTIMATE_SENT);
    }

    public long countDistancePending() {
        return reservationRepository.countByStatusInAndDistanceKmIsNull(List.of(
                ReservationStatus.RECEIVED,
                ReservationStatus.CONSULTING,
                ReservationStatus.ESTIMATE_SENT,
                ReservationStatus.CONFIRMED
        ));
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }

        String trimmedKeyword = keyword.trim();
        if (trimmedKeyword.isEmpty()) {
            return null;
        }

        return trimmedKeyword.toLowerCase();
    }

    private Comparator<Reservation> comparator(ReservationSort sort) {
        ReservationSort selectedSort = sort == null ? ReservationSort.PRIORITY : sort;

        return switch (selectedSort) {
            case PRIORITY -> Comparator
                    .comparingInt(this::priorityRank)
                    .thenComparing(Reservation::getMoveDate)
                    .thenComparing(Reservation::getMoveTime)
                    .thenComparing(Reservation::getId);
            case MOVE_DATE -> Comparator
                    .comparing(Reservation::getMoveDate)
                    .thenComparing(Reservation::getMoveTime)
                    .thenComparing(Reservation::getId);
            case CREATED_DESC -> Comparator
                    .comparing(Reservation::getCreatedAt, Comparator.reverseOrder())
                    .thenComparing(Reservation::getId, Comparator.reverseOrder());
        };
    }

    private int priorityRank(Reservation reservation) {
        return switch (reservation.getStatus()) {
            case RECEIVED -> 1;
            case CONSULTING -> 2;
            case ESTIMATE_SENT -> 3;
            case CONFIRMED -> 4;
            case COMPLETED -> 5;
            case CANCELED -> 6;
        };
    }
}
