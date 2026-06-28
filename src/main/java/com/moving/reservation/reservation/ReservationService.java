package com.moving.reservation.reservation;

import com.moving.reservation.availability.AvailabilityService;
import com.moving.reservation.coupon.Coupon;
import com.moving.reservation.coupon.CouponService;
import com.moving.reservation.notification.CustomerNotificationService;
import com.moving.reservation.review.ReviewService;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private static final List<ReservationStatus> ATTENTION_REQUIRED_STATUSES = List.of(
            ReservationStatus.RECEIVED,
            ReservationStatus.CONSULTING,
            ReservationStatus.ESTIMATE_SENT,
            ReservationStatus.CONFIRMED
    );

    private final ReservationRepository reservationRepository;
    private final ReservationStatusHistoryRepository statusHistoryRepository;
    private final ReservationPhotoRepository reservationPhotoRepository;
    private final ReservationCustomerActionHistoryRepository customerActionHistoryRepository;
    private final ReservationCustomerRequestRepository customerRequestRepository;
    private final ReservationPhotoStorage reservationPhotoStorage;
    private final CouponService couponService;
    private final ReviewService reviewService;
    private final ReservationEstimateCalculator estimateCalculator;
    private final ReservationConflictAttemptService conflictAttemptService;
    private final AvailabilityService availabilityService;
    private final boolean scheduleConflictEnabled;
    private final CustomerNotificationService customerNotificationService;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationStatusHistoryRepository statusHistoryRepository,
                              ReservationPhotoRepository reservationPhotoRepository,
                              ReservationCustomerActionHistoryRepository customerActionHistoryRepository,
                              ReservationCustomerRequestRepository customerRequestRepository,
                              ReservationPhotoStorage reservationPhotoStorage,
                              CouponService couponService,
                              ReviewService reviewService,
                              ReservationEstimateCalculator estimateCalculator,
                              ReservationConflictAttemptService conflictAttemptService,
                              AvailabilityService availabilityService,
                              @Value("${reservation.schedule-conflict.enabled:true}") boolean scheduleConflictEnabled,
                              CustomerNotificationService customerNotificationService) {
        this.reservationRepository = reservationRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.reservationPhotoRepository = reservationPhotoRepository;
        this.customerActionHistoryRepository = customerActionHistoryRepository;
        this.customerRequestRepository = customerRequestRepository;
        this.reservationPhotoStorage = reservationPhotoStorage;
        this.couponService = couponService;
        this.reviewService = reviewService;
        this.estimateCalculator = estimateCalculator;
        this.conflictAttemptService = conflictAttemptService;
        this.availabilityService = availabilityService;
        this.scheduleConflictEnabled = scheduleConflictEnabled;
        this.customerNotificationService = customerNotificationService;
    }

    @Transactional(noRollbackFor = ReservationScheduleConflictException.class)
    public Reservation create(ReservationCreateRequest request) {
        if (scheduleConflictEnabled) {
            try {
                availabilityService.ensureAvailable(request.getMoveDate(), request.getMoveTime());
            } catch (ReservationScheduleConflictException exception) {
                conflictAttemptService.record(request);
                throw exception;
            }
        }

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
        return search(status, keyword, startDate, endDate, needsDistance, false, sort);
    }

    public List<Reservation> search(ReservationStatus status,
                                    String keyword,
                                    LocalDate startDate,
                                    LocalDate endDate,
                                    Boolean needsDistance,
                                    Boolean attentionRequired,
                                    ReservationSort sort) {
        return reservationRepository.search(
                        status,
                        normalizeKeyword(keyword),
                        startDate,
                        endDate,
                        needsDistance,
                        attentionRequired,
                        ATTENTION_REQUIRED_STATUSES
                ).stream()
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
        return searchPage(status, keyword, startDate, endDate, needsDistance, false, sort, pageable);
    }

    public Page<Reservation> searchPage(ReservationStatus status,
                                        String keyword,
                                        LocalDate startDate,
                                        LocalDate endDate,
                                        Boolean needsDistance,
                                        Boolean attentionRequired,
                                        ReservationSort sort,
                                        Pageable pageable) {
        List<Reservation> reservations = search(status, keyword, startDate, endDate, needsDistance, attentionRequired, sort);
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

    @Transactional
    public List<ReservationPhoto> addPhotos(Long id, String phone, List<MultipartFile> itemPhotos) {
        Reservation reservation = reservationRepository.findByIdAndPhone(id, phone)
                .orElseThrow(() -> new IllegalArgumentException("예약 번호와 연락처가 일치하지 않습니다."));

        if (!reservation.isEditable()) {
            throw new IllegalArgumentException("현재 상태에서는 짐 사진을 업로드할 수 없습니다.");
        }

        List<MultipartFile> uploadFiles = itemPhotos == null ? List.of() : itemPhotos.stream()
                .filter(itemPhoto -> itemPhoto != null && !itemPhoto.isEmpty())
                .toList();

        if (uploadFiles.isEmpty()) {
            throw new IllegalArgumentException("업로드할 짐 사진을 선택해 주세요.");
        }

        return uploadFiles.stream()
                .map(reservationPhotoStorage::store)
                .map(storedPhoto -> new ReservationPhoto(
                        reservation,
                        storedPhoto.originalFilename(),
                        storedPhoto.storedFilename(),
                        storedPhoto.fileUrl()
                ))
                .map(reservationPhotoRepository::save)
                .toList();
    }

    public List<ReservationCustomerActionHistory> findCustomerActionHistories(Long reservationId) {
        return customerActionHistoryRepository.findByReservationIdOrderByCreatedAtDesc(reservationId);
    }

    public List<ReservationCustomerRequest> findCustomerRequests(Long reservationId) {
        return customerRequestRepository.findByReservationIdOrderByRequestedAtDesc(reservationId);
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
        requestCancel(id, phone);
    }

    @Transactional
    public void requestCancel(Long id, String phone) {
        Reservation reservation = reservationRepository.findByIdAndPhone(id, phone)
                .orElseThrow(() -> new IllegalArgumentException("예약 번호와 연락처가 일치하지 않습니다."));

        if (!reservation.isCancelable()) {
            throw new IllegalArgumentException("현재 상태에서는 예약을 취소할 수 없습니다.");
        }

        ensureNoPendingCustomerRequest(reservation);

        String detail = "취소 요청 당시 상태: " + reservation.getStatus().getLabel();
        customerRequestRepository.save(ReservationCustomerRequest.cancel(reservation, detail));
        customerActionHistoryRepository.save(new ReservationCustomerActionHistory(
                reservation,
                CustomerActionType.CANCEL_REQUEST,
                "고객이 예약 취소를 요청했습니다.",
                detail,
                "customer"
        ));
    }

    @Transactional
    public void acceptEstimate(Long id) {
        Reservation reservation = get(id);
        acceptEstimate(reservation);
    }

    @Transactional
    public void acceptEstimate(Long id, String phone) {
        Reservation reservation = reservationRepository.findByIdAndPhone(id, phone)
                .orElseThrow(() -> new IllegalArgumentException("예약 번호와 연락처가 일치하지 않습니다."));
        acceptEstimate(reservation);
    }

    private void acceptEstimate(Reservation reservation) {
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
        requestUpdateDetails(id, request);
    }

    @Transactional
    public void requestUpdateDetails(Long id, ReservationUpdateRequest request) {
        Reservation reservation = reservationRepository.findByIdAndPhone(id, request.getPhone())
                .orElseThrow(() -> new IllegalArgumentException("예약 번호와 연락처가 일치하지 않습니다."));

        if (!reservation.isEditable()) {
            throw new IllegalArgumentException("현재 상태에서는 예약을 수정할 수 없습니다.");
        }

        ensureNoPendingCustomerRequest(reservation);

        if (scheduleConflictEnabled) {
            availabilityService.ensureAvailable(request.getMoveDate(), request.getMoveTime(), reservation.getId());
        }

        String changeDetail = customerUpdateChangeDetail(reservation, request);
        customerRequestRepository.save(ReservationCustomerRequest.update(reservation, request, changeDetail));
        customerActionHistoryRepository.save(new ReservationCustomerActionHistory(
                reservation,
                CustomerActionType.UPDATE_REQUEST,
                "고객이 예약 수정 요청을 남겼습니다.",
                changeDetail,
                "customer"
        ));
    }

    @Transactional
    public ReservationCustomerRequest approveCustomerRequest(Long requestId, String processedBy) {
        ReservationCustomerRequest request = getCustomerRequest(requestId);
        Reservation reservation = request.getReservation();

        request.approve(processedBy);

        if (request.getRequestType() == CustomerRequestType.UPDATE) {
            approveUpdateRequest(request);
        } else if (request.getRequestType() == CustomerRequestType.CANCEL) {
            approveCancelRequest(request, processedBy);
        }

        customerActionHistoryRepository.save(new ReservationCustomerActionHistory(
                reservation,
                CustomerActionType.REQUEST_APPROVED,
                "관리자가 고객 요청을 승인했습니다.",
                request.getRequestType().getLabel() + "\n" + request.getDetail(),
                processedBy
        ));

        return request;
    }

    @Transactional
    public ReservationCustomerRequest rejectCustomerRequest(Long requestId, String processedBy, String rejectionReason) {
        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new IllegalArgumentException("반려 사유를 입력해 주세요.");
        }

        ReservationCustomerRequest request = getCustomerRequest(requestId);
        request.reject(processedBy, rejectionReason.trim());
        customerActionHistoryRepository.save(new ReservationCustomerActionHistory(
                request.getReservation(),
                CustomerActionType.REQUEST_REJECTED,
                "관리자가 고객 요청을 반려했습니다.",
                request.getRequestType().getLabel() + "\n반려 사유: " + rejectionReason.trim(),
                processedBy
        ));

        return request;
    }

    private ReservationCustomerRequest getCustomerRequest(Long requestId) {
        return customerRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("고객 요청을 찾을 수 없습니다."));
    }

    private void approveUpdateRequest(ReservationCustomerRequest request) {
        Reservation reservation = request.getReservation();

        if (!reservation.isEditable()) {
            throw new IllegalArgumentException("현재 상태에서는 예약 수정 요청을 승인할 수 없습니다.");
        }

        if (scheduleConflictEnabled) {
            availabilityService.ensureAvailable(request.getMoveDate(), request.getMoveTime(), reservation.getId());
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
        recalculateBaseEstimate(reservation);
    }

    private void approveCancelRequest(ReservationCustomerRequest request, String processedBy) {
        Reservation reservation = request.getReservation();

        if (!reservation.isCancelable()) {
            throw new IllegalArgumentException("현재 상태에서는 예약 취소 요청을 승인할 수 없습니다.");
        }

        changeStatus(reservation, ReservationStatus.CANCELED, processedBy);
    }

    private void ensureNoPendingCustomerRequest(Reservation reservation) {
        if (customerRequestRepository.existsByReservationIdAndStatus(reservation.getId(), CustomerRequestStatus.PENDING)) {
            throw new IllegalArgumentException("이미 처리 대기 중인 고객 요청이 있습니다.");
        }
    }

    @Transactional
    public void updateDistance(Long id, Integer distanceKm) {
        Reservation reservation = get(id);
        reservation.updateDistance(distanceKm);
        recalculateBaseEstimate(reservation);
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

    private String customerUpdateChangeDetail(Reservation reservation, ReservationUpdateRequest request) {
        List<String> changes = new java.util.ArrayList<>();
        addChange(changes, "이사일", reservation.getMoveDate(), request.getMoveDate());
        addChange(changes, "희망 시간", reservation.getMoveTime(), request.getMoveTime());
        addChange(changes, "출발 주소", reservation.getFromAddress(), request.getFromAddress());
        addChange(changes, "도착 주소", reservation.getToAddress(), request.getToAddress());
        addChange(changes, "출발지 층수", reservation.getFromFloor(), request.getFromFloor());
        addChange(changes, "도착지 층수", reservation.getToFloor(), request.getToFloor());
        addChange(changes, "출발지 사다리차", reservation.isFromLadderTruck() ? "필요" : "없음", request.isFromLadderTruck() ? "필요" : "없음");
        addChange(changes, "도착지 사다리차", reservation.isToLadderTruck() ? "필요" : "없음", request.isToLadderTruck() ? "필요" : "없음");
        addChange(changes, "이메일", emptyLabel(reservation.getEmail()), emptyLabel(request.getEmail()));
        addChange(changes, "요청사항", emptyLabel(reservation.getMemo()), emptyLabel(request.getMemo()));

        if (changes.isEmpty()) {
            return "변경된 항목은 없지만 고객이 예약 수정 저장을 요청했습니다.";
        }

        return String.join("\n", changes);
    }

    private void addChange(List<String> changes, String label, Object before, Object after) {
        if (!Objects.equals(before, after)) {
            changes.add(label + ": " + before + " -> " + after);
        }
    }

    private String emptyLabel(String value) {
        return value == null || value.isBlank() ? "미입력" : value.trim();
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

    public long countPendingCustomerRequests() {
        return customerRequestRepository.countByStatus(CustomerRequestStatus.PENDING);
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
