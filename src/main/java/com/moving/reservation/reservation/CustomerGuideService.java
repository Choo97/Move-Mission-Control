package com.moving.reservation.reservation;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerGuideService {

    private final CustomerGuideRepository customerGuideRepository;

    public CustomerGuideService(CustomerGuideRepository customerGuideRepository) {
        this.customerGuideRepository = customerGuideRepository;
    }

    public List<CustomerGuide> findAll() {
        return customerGuideRepository.findAllByOrderByStatusAscDisplayOrderAscIdAsc();
    }

    public List<CustomerGuideItem> findActiveItems(ReservationStatus status) {
        return customerGuideRepository.findByStatusAndActiveTrueOrderByDisplayOrderAscIdAsc(status)
                .stream()
                .map(CustomerGuide::toItem)
                .toList();
    }

    @Transactional
    public void create(CustomerGuideSaveRequest request) {
        customerGuideRepository.save(new CustomerGuide(
                request.getStatus(),
                request.getTitle().trim(),
                request.getDescription().trim(),
                request.getDisplayOrder()
        ));
    }

    @Transactional
    public void update(Long id, CustomerGuideSaveRequest request) {
        get(id).update(
                request.getStatus(),
                request.getTitle().trim(),
                request.getDescription().trim(),
                request.getDisplayOrder()
        );
    }

    @Transactional
    public void activate(Long id) {
        get(id).activate();
    }

    @Transactional
    public void deactivate(Long id) {
        get(id).deactivate();
    }

    @Transactional
    public void initializeDefaults() {
        if (customerGuideRepository.count() > 0) {
            return;
        }

        customerGuideRepository.saveAll(List.of(
                new CustomerGuide(ReservationStatus.RECEIVED, "연락 받을 준비", "상담 전화나 안내 메일을 확인할 수 있도록 연락처와 이메일을 확인해 주세요.", 1),
                new CustomerGuide(ReservationStatus.RECEIVED, "예약 정보 확인", "이사일, 출발지, 도착지, 층수 정보가 맞는지 다시 확인해 주세요.", 2),
                new CustomerGuide(ReservationStatus.RECEIVED, "짐 사진 준비", "짐 사진을 올렸다면 상담 때 더 빠르게 견적을 안내받을 수 있습니다.", 3),
                new CustomerGuide(ReservationStatus.CONSULTING, "현장 조건 확인", "엘리베이터 사용 가능 여부, 주차 위치, 사다리차 필요 여부를 확인해 주세요.", 1),
                new CustomerGuide(ReservationStatus.CONSULTING, "짐 양 설명", "큰 가구, 가전, 분해가 필요한 물건이 있으면 상담 때 알려 주세요.", 2),
                new CustomerGuide(ReservationStatus.CONSULTING, "견적 안내 대기", "상담 내용이 정리되면 최종 견적을 안내해 드립니다.", 3),
                new CustomerGuide(ReservationStatus.ESTIMATE_SENT, "최종 견적 확인", "안내된 최종 견적 금액과 산정 내역을 확인해 주세요.", 1),
                new CustomerGuide(ReservationStatus.ESTIMATE_SENT, "견적 동의 진행", "금액이 맞으면 견적 동의 버튼으로 예약을 확정할 수 있습니다.", 2),
                new CustomerGuide(ReservationStatus.ESTIMATE_SENT, "변경 사항 문의", "주소, 날짜, 짐 양이 바뀌면 동의 전 문의해 주세요.", 3),
                new CustomerGuide(ReservationStatus.CONFIRMED, "이사 전 정리", "파손 위험 물건과 귀중품은 따로 분류해 두면 당일 작업이 빨라집니다.", 1),
                new CustomerGuide(ReservationStatus.CONFIRMED, "출입 동선 확인", "주차 공간, 공동현관, 엘리베이터 사용 시간을 미리 확인해 주세요.", 2),
                new CustomerGuide(ReservationStatus.CONFIRMED, "당일 연락 확인", "이사 당일 연락 가능한 휴대폰을 확인하고 대기해 주세요.", 3),
                new CustomerGuide(ReservationStatus.COMPLETED, "리뷰 작성", "서비스 이용 후 느낀 점을 남기면 운영 품질 개선에 도움이 됩니다.", 1),
                new CustomerGuide(ReservationStatus.COMPLETED, "예약 내역 보관", "견적 확정서와 예약 정보를 필요할 때 다시 확인할 수 있습니다.", 2),
                new CustomerGuide(ReservationStatus.COMPLETED, "추가 문의", "추가 정리나 문의가 있으면 예약 정보를 기준으로 다시 상담할 수 있습니다.", 3),
                new CustomerGuide(ReservationStatus.CANCELED, "취소 내역 확인", "취소된 예약은 현재 상태와 예약 정보를 조회용으로만 확인할 수 있습니다.", 1),
                new CustomerGuide(ReservationStatus.CANCELED, "새 예약 신청", "이사가 다시 필요하면 메인 화면에서 새 예약을 신청해 주세요.", 2),
                new CustomerGuide(ReservationStatus.CANCELED, "문의 필요 시 연락", "취소 사유나 재예약 조건을 확인해야 하면 관리자에게 문의해 주세요.", 3)
        ));
    }

    private CustomerGuide get(Long id) {
        return customerGuideRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("고객 안내를 찾을 수 없습니다."));
    }
}
