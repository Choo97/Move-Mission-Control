package com.moving.reservation.admin;

import com.moving.reservation.reservation.Reservation;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AdminReservationCsvExporter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String[] HEADERS = {
            "예약번호",
            "고객명",
            "연락처",
            "이메일",
            "이사일",
            "이사시간",
            "상태",
            "서비스유형",
            "이사유형",
            "출발주소",
            "도착주소",
            "이동거리(km)",
            "견적금액",
            "할인금액",
            "최종견적",
            "쿠폰코드",
            "고객동의금액",
            "접수일시",
            "관리자메모"
    };

    public byte[] export(List<Reservation> reservations) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        appendRow(csv, HEADERS);

        for (Reservation reservation : reservations) {
            appendRow(csv,
                    value(reservation.getId()),
                    reservation.getCustomerName(),
                    reservation.getPhone(),
                    reservation.getEmail(),
                    value(reservation.getMoveDate()),
                    value(reservation.getMoveTime()),
                    reservation.getStatus().getLabel(),
                    reservation.getServiceType().getLabel(),
                    reservation.getMoveType().getLabel(),
                    reservation.getFromAddress(),
                    reservation.getToAddress(),
                    value(reservation.getDistanceKm()),
                    value(reservation.getEstimatedPrice()),
                    value(reservation.getAppliedDiscountAmount()),
                    value(reservation.getFinalEstimatedPrice()),
                    reservation.getCouponCode(),
                    value(reservation.getAcceptedEstimatePrice()),
                    reservation.getCreatedAt() == null ? "" : reservation.getCreatedAt().format(DATE_TIME_FORMATTER),
                    reservation.getAdminMemo()
            );
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void appendRow(StringBuilder csv, String... values) {
        for (int index = 0; index < values.length; index++) {
            if (index > 0) {
                csv.append(',');
            }

            csv.append(escape(values[index]));
        }

        csv.append("\r\n");
    }

    private String escape(String value) {
        String safeValue = value == null ? "" : value;

        if (!safeValue.isEmpty() && "=+-@\t\r".indexOf(safeValue.charAt(0)) >= 0) {
            safeValue = "'" + safeValue;
        }

        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
