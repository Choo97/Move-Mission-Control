package com.moving.reservation.privacy;

import static org.assertj.core.api.Assertions.assertThat;

import com.moving.reservation.reservation.MoveType;
import com.moving.reservation.reservation.Reservation;
import com.moving.reservation.reservation.ReservationCreateRequest;
import com.moving.reservation.reservation.ReservationRepository;
import com.moving.reservation.reservation.ReservationSearchRequest;
import com.moving.reservation.reservation.ReservationService;
import com.moving.reservation.reservation.ReservationUpdateRequest;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationPrivacyEncryptionTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 예약_민감정보는_DB에_암호문과_연락처해시로_저장된다() {
        Reservation reservation = reservationService.create(reservationCreateRequest());
        entityManager.flush();

        Map<String, Object> row = jdbcTemplate.queryForMap("""
                select customer_name, phone, phone_hash, email, from_address, to_address, memo
                from reservation
                where id = ?
                """, reservation.getId());

        assertEncrypted(row.get("customer_name"), "홍길동");
        assertEncrypted(row.get("phone"), "010-1234-5678");
        assertEncrypted(row.get("email"), "customer@example.com");
        assertEncrypted(row.get("from_address"), "서울시 강남구 테헤란로 1");
        assertEncrypted(row.get("to_address"), "서울시 송파구 올림픽로 1");
        assertEncrypted(row.get("memo"), "테스트 예약입니다.");
        assertThat(row.get("phone_hash").toString())
                .hasSize(64)
                .doesNotContain("010")
                .doesNotContain("1234")
                .doesNotContain("5678");

        ReservationSearchRequest searchRequest = new ReservationSearchRequest();
        searchRequest.setReservationId(reservation.getId());
        searchRequest.setPhone("010-1234-5678");

        assertThat(reservationService.search(searchRequest).getCustomerName()).isEqualTo("홍길동");
    }

    @Test
    void 고객요청과_알림_민감정보도_DB에_암호문으로_저장된다() {
        Reservation reservation = reservationService.create(reservationCreateRequest());
        reservationService.requestUpdateDetails(reservation.getId(), reservationUpdateRequest());
        entityManager.flush();

        Map<String, Object> customerRequestRow = jdbcTemplate.queryForMap("""
                select email, from_address, to_address, memo, detail
                from reservation_customer_request
                where reservation_id = ?
                """, reservation.getId());
        assertEncrypted(customerRequestRow.get("email"), "updated@example.com");
        assertEncrypted(customerRequestRow.get("from_address"), "서울시 마포구 월드컵북로 1");
        assertEncrypted(customerRequestRow.get("to_address"), "서울시 용산구 한강대로 1");
        assertEncrypted(customerRequestRow.get("memo"), "수정된 테스트 예약입니다.");
        assertEncrypted(customerRequestRow.get("detail"), "출발 주소");

        Map<String, Object> notificationRow = jdbcTemplate.queryForMap("""
                select recipient_phone, message
                from customer_notification
                where reservation_id = ?
                order by id asc
                limit 1
                """, reservation.getId());
        assertEncrypted(notificationRow.get("recipient_phone"), "010-1234-5678");
        assertEncrypted(notificationRow.get("message"), "예약 번호");
    }

    @Test
    void 연락처해시가_없는_기존예약도_연락처로_조회할_수_있다() {
        Reservation legacyReservation = reservationRepository.save(new Reservation(
                "기존고객",
                "010-9999-8888",
                "legacy@example.com",
                LocalDate.now().plusDays(5),
                LocalTime.of(9, 0),
                "서울시 강남구 테헤란로 1",
                "서울시 송파구 올림픽로 1",
                MoveType.STUDIO,
                true,
                true,
                3,
                5,
                false,
                false,
                "기존 예약입니다."
        ));

        ReservationSearchRequest searchRequest = new ReservationSearchRequest();
        searchRequest.setReservationId(legacyReservation.getId());
        searchRequest.setPhone("010-9999-8888");

        assertThat(reservationService.search(searchRequest).getCustomerName()).isEqualTo("기존고객");
    }

    private void assertEncrypted(Object databaseValue, String plainText) {
        assertThat(databaseValue).isNotNull();
        assertThat(databaseValue.toString())
                .startsWith("enc:v1:")
                .doesNotContain(plainText);
    }

    private ReservationCreateRequest reservationCreateRequest() {
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setCustomerName("홍길동");
        request.setPhone("010-1234-5678");
        request.setEmail("customer@example.com");
        request.setMoveDate(LocalDate.now().plusDays(7));
        request.setMoveTime(LocalTime.of(10, 30));
        request.setFromAddress("서울시 강남구 테헤란로 1");
        request.setToAddress("서울시 송파구 올림픽로 1");
        request.setMoveType(MoveType.STUDIO);
        request.setFromElevator(true);
        request.setToElevator(true);
        request.setFromFloor(3);
        request.setToFloor(5);
        request.setMemo("테스트 예약입니다.");
        return request;
    }

    private ReservationUpdateRequest reservationUpdateRequest() {
        ReservationUpdateRequest request = new ReservationUpdateRequest();
        request.setPhone("010-1234-5678");
        request.setEmail("updated@example.com");
        request.setMoveDate(LocalDate.now().plusDays(14));
        request.setMoveTime(LocalTime.of(14, 0));
        request.setFromAddress("서울시 마포구 월드컵북로 1");
        request.setToAddress("서울시 용산구 한강대로 1");
        request.setFromFloor(2);
        request.setToFloor(4);
        request.setFromLadderTruck(false);
        request.setToLadderTruck(true);
        request.setMemo("수정된 테스트 예약입니다.");
        return request;
    }
}
