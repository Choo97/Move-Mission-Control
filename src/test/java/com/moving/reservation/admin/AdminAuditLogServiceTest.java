package com.moving.reservation.admin;

import static org.assertj.core.api.Assertions.assertThat;

import com.moving.reservation.reservation.MoveType;
import com.moving.reservation.reservation.Reservation;
import com.moving.reservation.reservation.ReservationCreateRequest;
import com.moving.reservation.reservation.ReservationService;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminAuditLogServiceTest {

    @Autowired
    private AdminAuditLogService adminAuditLogService;

    @Autowired
    private ReservationService reservationService;

    @Test
    void 관리자_작업을_감사로그로_저장하고_예약별로_조회한다() {
        Reservation reservation = reservationService.create(reservationCreateRequest("010-1234-5678"));

        adminAuditLogService.record(
                reservation,
                "예약 상태 변경",
                "예약 상태를 '상담중'(으)로 변경했습니다.",
                "admin"
        );

        assertThat(adminAuditLogService.findByReservationId(reservation.getId()))
                .singleElement()
                .satisfies(auditLog -> {
                    assertThat(auditLog.getReservation().getId()).isEqualTo(reservation.getId());
                    assertThat(auditLog.getAction()).isEqualTo("예약 상태 변경");
                    assertThat(auditLog.getDetail()).contains("상담중");
                    assertThat(auditLog.getCreatedBy()).isEqualTo("admin");
                    assertThat(auditLog.getCreatedAt()).isNotNull();
                });
    }

    @Test
    void 작성자가_비어있으면_system으로_저장한다() {
        Reservation reservation = reservationService.create(reservationCreateRequest("010-1234-5678"));

        adminAuditLogService.record(reservation, "예약 목록 CSV 다운로드", "CSV 파일을 다운로드했습니다.", " ");

        assertThat(adminAuditLogService.findByReservationId(reservation.getId()))
                .singleElement()
                .extracting(AdminAuditLog::getCreatedBy)
                .isEqualTo("system");
    }

    @Test
    void 작업명_작성자_키워드_기간으로_감사로그를_검색한다() {
        Reservation firstReservation = reservationService.create(reservationCreateRequest("010-1111-1111"));
        Reservation secondReservation = reservationService.create(reservationCreateRequest("010-2222-2222"));
        adminAuditLogService.record(firstReservation, "견적 금액 저장", "견적 금액을 180,000원으로 저장했습니다.", "admin");
        adminAuditLogService.record(secondReservation, "관리자 메모 저장", "고객 통화 내용을 저장했습니다.", "manager");

        assertThat(adminAuditLogService.search(
                "견적 금액 저장",
                "adm",
                "180,000",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
        ))
                .singleElement()
                .satisfies(auditLog -> {
                    assertThat(auditLog.getReservation().getId()).isEqualTo(firstReservation.getId());
                    assertThat(auditLog.getAction()).isEqualTo("견적 금액 저장");
                    assertThat(auditLog.getCreatedBy()).isEqualTo("admin");
                });
    }

    @Test
    void 감사로그_작업명_목록을_중복없이_정렬해_조회한다() {
        Reservation reservation = reservationService.create(reservationCreateRequest("010-1234-5678"));
        adminAuditLogService.record(reservation, "관리자 메모 저장", "메모를 저장했습니다.", "admin");
        adminAuditLogService.record(reservation, "견적 금액 저장", "견적을 저장했습니다.", "admin");
        adminAuditLogService.record(reservation, "관리자 메모 저장", "메모를 다시 저장했습니다.", "admin");

        assertThat(adminAuditLogService.findActions())
                .containsExactly("견적 금액 저장", "관리자 메모 저장");
    }

    private ReservationCreateRequest reservationCreateRequest(String phone) {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setCustomerName("감사로그고객");
        request.setPhone(phone);
        request.setEmail("audit@example.com");
        request.setMoveDate(LocalDate.now().plusDays(7));
        request.setMoveTime(LocalTime.of(10, 30));
        request.setFromAddress("서울시 강남구 테헤란로 1");
        request.setToAddress("서울시 송파구 올림픽로 1");
        request.setMoveType(MoveType.STUDIO);
        request.setFromElevator(true);
        request.setToElevator(true);
        request.setFromFloor(3);
        request.setToFloor(5);
        request.setMemo("감사 로그 테스트 예약입니다.");
        return request;
    }
}
