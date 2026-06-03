package com.moving.reservation.admin;

import com.moving.reservation.reservation.Reservation;
import com.moving.reservation.reservation.ReservationEstimateLine;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Service;

@Service
public class EstimateDocumentPdfService {

    private static final Path MALGUN_FONT_PATH = Path.of("C:/Windows/Fonts/malgun.ttf");
    private static final float PAGE_MARGIN = 50;
    private static final float LINE_HEIGHT = 18;
    private static final float SECTION_GAP = 18;
    private static final float LABEL_X = 70;
    private static final float VALUE_X = 210;

    public byte[] generate(Reservation reservation, List<ReservationEstimateLine> estimateLines) {
        try (PDDocument document = new PDDocument()) {
            PDType0Font font = loadKoreanFont(document);
            PdfWriter writer = new PdfWriter(document, font);

            writer.writeTitle("견적 확정서");
            writer.writeText("예약 번호 " + reservation.getId() + "번 이사 예약의 견적 산정 내용입니다.", 11);

            writer.writeSection("고객 정보");
            writer.writeKeyValue("고객명", reservation.getCustomerName());
            writer.writeKeyValue("연락처", reservation.getPhone());
            writer.writeKeyValue("이사 일정", reservation.getMoveDate() + " " + reservation.getMoveTime());
            writer.writeKeyValue("이사 유형", reservation.getMoveType().getLabel());

            writer.writeSection("이동 정보");
            writer.writeKeyValue("출발지", reservation.getFromAddress());
            writer.writeKeyValue("도착지", reservation.getToAddress());
            writer.writeKeyValue("층수", "출발지 " + floor(reservation.getFromFloor()) + "층 / 도착지 "
                    + floor(reservation.getToFloor()) + "층");
            writer.writeKeyValue("현장 조건", "엘리베이터 출발지 " + yesNo(reservation.isFromElevator())
                    + " / 도착지 " + yesNo(reservation.isToElevator())
                    + " / 사다리차 출발지 " + needNo(reservation.isFromLadderTruck())
                    + " / 도착지 " + needNo(reservation.isToLadderTruck()));
            writer.writeKeyValue("이동 거리", reservation.getDistanceKm() == null ? "확인 전" : reservation.getDistanceKm() + "km");

            writer.writeSection("견적 산정 내역");
            for (ReservationEstimateLine line : estimateLines) {
                writer.writeKeyValue(line.label(), money(line.amount()));
            }

            writer.writeSection("최종 견적");
            writer.writeKeyValue("기본 견적", reservation.getBaseEstimatedPrice() == null ? "계산 전" : money(reservation.getBaseEstimatedPrice()));
            writer.writeKeyValue("할인", money(reservation.getAppliedDiscountAmount()));
            writer.writeKeyValue("최종 견적", reservation.getFinalEstimatedPrice() == null ? "상담 후 안내" : money(reservation.getFinalEstimatedPrice()));

            writer.writeSection("안내 사항");
            writer.writeText("현장 상황, 추가 짐, 주차 여건, 고객 요청 변경에 따라 최종 금액은 조정될 수 있습니다.", 11);
            writer.close();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("견적 확정서 PDF를 생성할 수 없습니다.", exception);
        }
    }

    private PDType0Font loadKoreanFont(PDDocument document) throws IOException {
        if (!Files.exists(MALGUN_FONT_PATH)) {
            throw new IllegalStateException("PDF 한글 폰트를 찾을 수 없습니다: " + MALGUN_FONT_PATH);
        }

        return PDType0Font.load(document, MALGUN_FONT_PATH.toFile());
    }

    private int floor(Integer floor) {
        return floor == null || floor < 1 ? 1 : floor;
    }

    private String yesNo(boolean value) {
        return value ? "있음" : "없음";
    }

    private String needNo(boolean value) {
        return value ? "필요" : "없음";
    }

    private String money(int amount) {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(amount) + "원";
    }

    private static class PdfWriter {

        private final PDDocument document;
        private final PDType0Font font;
        private PDPage page;
        private PDPageContentStream contentStream;
        private float y;

        private PdfWriter(PDDocument document, PDType0Font font) throws IOException {
            this.document = document;
            this.font = font;
            addPage();
        }

        private void writeTitle(String text) throws IOException {
            ensureSpace(48);
            writeAt(text, PAGE_MARGIN, y, 28);
            y -= 38;
        }

        private void writeSection(String text) throws IOException {
            ensureSpace(44);
            y -= SECTION_GAP;
            writeAt(text, PAGE_MARGIN, y, 16);
            y -= 24;
        }

        private void writeKeyValue(String label, String value) throws IOException {
            ensureSpace(LINE_HEIGHT);
            writeAt(label, LABEL_X, y, 11);
            writeAt(value, VALUE_X, y, 11);
            y -= LINE_HEIGHT;
        }

        private void writeText(String text, int fontSize) throws IOException {
            ensureSpace(LINE_HEIGHT);
            writeAt(text, PAGE_MARGIN, y, fontSize);
            y -= LINE_HEIGHT;
        }

        private void writeAt(String text, float x, float y, int fontSize) throws IOException {
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(text == null ? "" : text);
            contentStream.endText();
        }

        private void ensureSpace(float neededHeight) throws IOException {
            if (y - neededHeight < PAGE_MARGIN) {
                addPage();
            }
        }

        private void addPage() throws IOException {
            if (contentStream != null) {
                contentStream.close();
            }

            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);
            y = page.getMediaBox().getHeight() - PAGE_MARGIN;
        }

        private void close() throws IOException {
            if (contentStream != null) {
                contentStream.close();
            }
        }
    }
}
