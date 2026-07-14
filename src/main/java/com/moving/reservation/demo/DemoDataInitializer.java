package com.moving.reservation.demo;

import com.moving.reservation.coupon.Coupon;
import com.moving.reservation.coupon.CouponRepository;
import com.moving.reservation.coupon.DiscountType;
import com.moving.reservation.privacy.PrivacyHashService;
import com.moving.reservation.reservation.MoveType;
import com.moving.reservation.reservation.Reservation;
import com.moving.reservation.reservation.ReservationRepository;
import com.moving.reservation.reservation.ReservationStatus;
import com.moving.reservation.reservation.ReservationStatusHistory;
import com.moving.reservation.reservation.ReservationStatusHistoryRepository;
import com.moving.reservation.reservation.ServiceRequestType;
import com.moving.reservation.review.Review;
import com.moving.reservation.review.ReviewRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "demo.seed", name = "enabled", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final ReservationRepository reservationRepository;
    private final ReservationStatusHistoryRepository statusHistoryRepository;
    private final ReviewRepository reviewRepository;
    private final CouponRepository couponRepository;
    private final PrivacyHashService privacyHashService;

    public DemoDataInitializer(ReservationRepository reservationRepository,
                               ReservationStatusHistoryRepository statusHistoryRepository,
                               ReviewRepository reviewRepository,
                               CouponRepository couponRepository,
                               PrivacyHashService privacyHashService) {
        this.reservationRepository = reservationRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.reviewRepository = reviewRepository;
        this.couponRepository = couponRepository;
        this.privacyHashService = privacyHashService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (reservationRepository.count() > 0) {
            log.info("데모 시드를 건너뜁니다. 기존 예약 데이터가 있습니다.");
            return;
        }

        Coupon demoCoupon = couponRepository.findByCode("DEMO10")
                .orElseGet(() -> couponRepository.save(
                        new Coupon("DEMO10", "포트폴리오 첫 예약 10%", DiscountType.PERCENT, 10)
                ));
        LocalDate today = LocalDate.now();

        seed(new DemoReservation(
                "일반고객01", "010-0000-1001", "general01@example.com",
                today.plusDays(3), LocalTime.of(9, 0),
                "서울시 가상구 일반로 1", "서울시 가상구 새집로 1",
                MoveType.STUDIO, ServiceRequestType.GENERAL, ReservationStatus.RECEIVED,
                true, true, 3, 7, false, false, null,
                "원룸 이사 상담을 요청한 가상 데이터입니다.", "신규 접수 확인 필요", false, null, null
        ), demoCoupon);
        seed(new DemoReservation(
                "일반고객02", "010-0000-1002", "general02@example.com",
                today.plusDays(8), LocalTime.of(10, 0),
                "인천시 가상구 출발로 2", "경기도 가상시 도착로 2",
                MoveType.TWO_ROOM, ServiceRequestType.GENERAL, ReservationStatus.CONSULTING,
                false, true, 4, 9, false, false, 11,
                "큰 책장이 있어 분해 가능 여부를 확인해 주세요.", "가구 분해 작업 상담 중", false, null, null
        ), demoCoupon);
        seed(new DemoReservation(
                "일반고객03", "010-0000-1003", "general03@example.com",
                today.plusDays(12), LocalTime.of(11, 0),
                "경기도 가상시 가족로 3", "서울시 가상구 보금자리로 3",
                MoveType.FAMILY, ServiceRequestType.GENERAL, ReservationStatus.ESTIMATE_SENT,
                true, false, 8, 5, false, true, 18,
                "가전과 대형 가구가 포함된 가정 이사입니다.", "최종 견적 동의 대기", true, null, null
        ), demoCoupon);
        seed(new DemoReservation(
                "일반고객04", "010-0000-1004", "general04@example.com",
                today.plusDays(18), LocalTime.of(13, 0),
                "서울시 가상구 업무로 4", "서울시 가상구 사무로 4",
                MoveType.OFFICE, ServiceRequestType.GENERAL, ReservationStatus.CONFIRMED,
                true, true, 6, 10, false, false, 25,
                "업무 공백을 줄이기 위해 오후 작업을 희망합니다.", "작업 인력과 차량 배정 완료", false, null, null
        ), demoCoupon);
        seed(new DemoReservation(
                "일반고객05", "010-0000-1005", "general05@example.com",
                today.minusDays(9), LocalTime.of(10, 0),
                "서울시 가상구 이전로 5", "서울시 가상구 완료로 5",
                MoveType.STUDIO, ServiceRequestType.GENERAL, ReservationStatus.COMPLETED,
                true, true, 2, 4, false, false, 6,
                "완료 흐름과 리뷰를 보여주는 가상 데이터입니다.", "이사 완료 및 리뷰 등록", false,
                5, "시간 약속을 잘 지켜 주고 짐도 안전하게 옮겨 주셨어요."
        ), demoCoupon);
        seed(new DemoReservation(
                "일반고객06", "010-0000-1006", "general06@example.com",
                today.plusDays(5), LocalTime.of(14, 0),
                "경기도 가상시 보관로 6", "경기도 가상시 반환로 6",
                MoveType.STORAGE, ServiceRequestType.GENERAL, ReservationStatus.CANCELED,
                false, false, 3, 3, false, false, null,
                "일정 변경으로 취소된 보관 이사 가상 데이터입니다.", "고객 일정 변경으로 취소", false, null, null
        ), demoCoupon);

        seed(new DemoReservation(
                "도움요청01", "010-0000-2001", "help01@example.com",
                today.plusDays(4), LocalTime.of(9, 0),
                "서울시 가상구 도움로 1", "서울시 가상구 자립로 1",
                MoveType.STUDIO, ServiceRequestType.NON_PROFIT, ReservationStatus.RECEIVED,
                false, true, 3, 5, false, false, null,
                "가상 사례: 자립을 준비하며 소형 이사 도움이 필요합니다.", "지원 가능 여부와 일정 확인 필요", false, null, null
        ), demoCoupon);
        seed(new DemoReservation(
                "도움요청02", "010-0000-2002", null,
                today.plusDays(9), LocalTime.of(10, 0),
                "경기도 가상시 이웃로 2", "경기도 가상시 안심로 2",
                MoveType.TWO_ROOM, ServiceRequestType.NON_PROFIT, ReservationStatus.CONSULTING,
                true, false, 7, 2, false, false, 9,
                "가상 사례: 돌봄 가정의 이사 일정과 필요한 인력을 상담 중입니다.", "협력 봉사자 일정 확인 중", false, null, null
        ), demoCoupon);
        seed(new DemoReservation(
                "도움요청03", "010-0000-2003", "help03@example.com",
                today.plusDays(16), LocalTime.of(13, 0),
                "인천시 가상구 동행로 3", "서울시 가상구 희망로 3",
                MoveType.FAMILY, ServiceRequestType.NON_PROFIT, ReservationStatus.CONFIRMED,
                true, true, 5, 8, false, false, 21,
                "가상 사례: 지역 복지기관 연계로 이사 도움을 요청했습니다.", "기관 확인 완료, 지원 일정 확정", false, null, null
        ), demoCoupon);
        seed(new DemoReservation(
                "도움요청04", "010-0000-2004", "help04@example.com",
                today.minusDays(6), LocalTime.of(11, 0),
                "서울시 가상구 나눔로 4", "서울시 가상구 새출발로 4",
                MoveType.STUDIO, ServiceRequestType.NON_PROFIT, ReservationStatus.COMPLETED,
                false, true, 4, 6, false, false, 8,
                "가상 사례: 긴급 주거 이전 지원을 완료한 요청입니다.", "도움 지원 완료 및 안부 확인", false,
                5, "막막했던 상황에서 친절하게 함께해 주셔서 큰 힘이 되었습니다."
        ), demoCoupon);
        seed(new DemoReservation(
                "도움요청05", "010-0000-2005", null,
                today.plusDays(7), LocalTime.of(15, 0),
                "경기도 가상시 연결로 5", "경기도 가상시 희망로 5",
                MoveType.STUDIO, ServiceRequestType.NON_PROFIT, ReservationStatus.CANCELED,
                true, true, 2, 3, false, false, null,
                "가상 사례: 다른 기관 지원이 확정되어 요청을 취소했습니다.", "타 기관 연계 완료로 종료", false, null, null
        ), demoCoupon);
        seed(new DemoReservation(
                "도움요청06", "010-0000-2006", "help06@example.com",
                today.plusDays(22), LocalTime.of(14, 0),
                "충청도 가상시 임시로 6", "경기도 가상시 정착로 6",
                MoveType.STORAGE, ServiceRequestType.NON_PROFIT, ReservationStatus.RECEIVED,
                false, false, 3, 4, false, false, 120,
                "가상 사례: 임시 보관 후 새 거처로 옮기는 장거리 도움이 필요합니다.", "장거리 지원 자원 확인 필요", false, null, null
        ), demoCoupon);

        log.info("일반 이사 6건과 비영리 도움 요청 6건의 데모 시드를 생성했습니다.");
    }

    private void seed(DemoReservation demo, Coupon demoCoupon) {
        Reservation reservation = new Reservation(
                demo.customerName(), demo.phone(), demo.email(), demo.moveDate(), demo.moveTime(),
                demo.fromAddress(), demo.toAddress(), demo.moveType(), demo.serviceType(),
                demo.fromElevator(), demo.toElevator(), demo.fromFloor(), demo.toFloor(),
                demo.fromLadderTruck(), demo.toLadderTruck(), demo.memo()
        );
        reservation.updatePhoneHash(privacyHashService.phoneHash(demo.phone()));
        reservation.updateDistance(demo.distanceKm());
        reservation.updateAdminMemo(demo.adminMemo(), "demo-seed");

        if (!demo.serviceType().isNonProfit()) {
            reservation.applyBaseEstimate(demo.moveType().getBasePrice());
            if (demo.applyCoupon()) {
                reservation.applyCoupon(demoCoupon);
            }
        }

        reservationRepository.save(reservation);
        applyStatusPath(reservation, demo.status());

        if (demo.rating() != null && demo.reviewContent() != null) {
            Review review = new Review(reservation, demo.rating(), demo.reviewContent());
            review.updateAdminReply("소중한 후기를 남겨 주셔서 감사합니다.", "demo-admin");
            reviewRepository.save(review);
        }
    }

    private void applyStatusPath(Reservation reservation, ReservationStatus targetStatus) {
        List<ReservationStatus> path = switch (targetStatus) {
            case RECEIVED -> List.of();
            case CONSULTING -> List.of(ReservationStatus.CONSULTING);
            case ESTIMATE_SENT -> List.of(ReservationStatus.CONSULTING, ReservationStatus.ESTIMATE_SENT);
            case CONFIRMED -> List.of(
                    ReservationStatus.CONSULTING,
                    ReservationStatus.ESTIMATE_SENT,
                    ReservationStatus.CONFIRMED
            );
            case COMPLETED -> List.of(
                    ReservationStatus.CONSULTING,
                    ReservationStatus.ESTIMATE_SENT,
                    ReservationStatus.CONFIRMED,
                    ReservationStatus.COMPLETED
            );
            case CANCELED -> List.of(ReservationStatus.CANCELED);
        };

        ReservationStatus previous = ReservationStatus.RECEIVED;
        for (ReservationStatus next : path) {
            if (next == ReservationStatus.CONFIRMED
                    && !reservation.getServiceType().isNonProfit()
                    && reservation.getFinalEstimatedPrice() != null) {
                reservation.acceptEstimate();
            }
            reservation.updateStatus(next);
            statusHistoryRepository.save(new ReservationStatusHistory(
                    reservation, previous, next, "demo-seed"
            ));
            previous = next;
        }
    }

    private record DemoReservation(
            String customerName,
            String phone,
            String email,
            LocalDate moveDate,
            LocalTime moveTime,
            String fromAddress,
            String toAddress,
            MoveType moveType,
            ServiceRequestType serviceType,
            ReservationStatus status,
            boolean fromElevator,
            boolean toElevator,
            int fromFloor,
            int toFloor,
            boolean fromLadderTruck,
            boolean toLadderTruck,
            Integer distanceKm,
            String memo,
            String adminMemo,
            boolean applyCoupon,
            Integer rating,
            String reviewContent
    ) {
    }
}
