package com.moving.reservation.faq;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FaqService {

    private final FaqRepository faqRepository;

    public FaqService(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    public List<Faq> findAll() {
        return faqRepository.findAllByOrderByDisplayOrderAscIdAsc();
    }

    public List<Faq> findActive() {
        return faqRepository.findByActiveTrueOrderByDisplayOrderAscIdAsc();
    }

    @Transactional
    public void create(FaqSaveRequest request) {
        faqRepository.save(new Faq(
                request.getQuestion().trim(),
                request.getAnswer().trim(),
                request.getDisplayOrder()
        ));
    }

    @Transactional
    public void update(Long id, FaqSaveRequest request) {
        get(id).update(
                request.getQuestion().trim(),
                request.getAnswer().trim(),
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
        if (faqRepository.count() > 0) {
            return;
        }

        faqRepository.saveAll(List.of(
                new Faq("예약 후에는 어떻게 확인하나요?",
                        "예약 완료 화면에 표시되는 예약 번호와 입력한 연락처로 예약조회 화면에서 확인할 수 있습니다.", 1),
                new Faq("견적은 바로 확정되나요?",
                        "기본 견적은 자동으로 계산되지만, 실제 작업 조건에 따라 관리자가 최종 견적을 조정할 수 있습니다.", 2),
                new Faq("짐 사진은 왜 업로드하나요?",
                        "짐의 양과 특이사항을 미리 확인하면 상담과 견적 안내가 더 정확해집니다.", 3),
                new Faq("예약 수정이나 취소는 언제까지 가능한가요?",
                        "예약 상태가 접수 또는 상담중일 때 고객 화면에서 수정과 취소가 가능합니다.", 4),
                new Faq("이메일 안내를 받으려면 어떻게 해야 하나요?",
                        "예약 신청 시 이메일을 입력하면 예약 확인 안내 메일 발송 대상으로 등록됩니다.", 5),
                new Faq("쿠폰은 어디에 입력하나요?",
                        "예약 신청 화면의 쿠폰 입력란에 쿠폰 코드를 입력하면 견적에 할인이 반영됩니다.", 6)
        ));
    }

    private Faq get(Long id) {
        return faqRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FAQ를 찾을 수 없습니다."));
    }
}
