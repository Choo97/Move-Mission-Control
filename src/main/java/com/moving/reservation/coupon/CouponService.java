package com.moving.reservation.coupon;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public List<Coupon> findAll() {
        return couponRepository.findAllByOrderByCreatedAtDesc();
    }

    public Coupon findActiveByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        return couponRepository.findByCode(code.trim().toUpperCase())
                .filter(Coupon::isActive)
                .orElseThrow(() -> new IllegalArgumentException("사용 가능한 쿠폰을 찾을 수 없습니다."));
    }

    @Transactional
    public void create(CouponCreateRequest request) {
        String code = request.getCode().trim().toUpperCase();

        if (couponRepository.existsByCode(code)) {
            throw new IllegalArgumentException("이미 사용 중인 쿠폰 코드입니다.");
        }

        if (request.getDiscountType() == DiscountType.PERCENT && request.getDiscountValue() > 100) {
            throw new IllegalArgumentException("정률 할인은 100% 이하로 입력해 주세요.");
        }

        couponRepository.save(new Coupon(
                code,
                request.getName().trim(),
                request.getDiscountType(),
                request.getDiscountValue()
        ));
    }

    @Transactional
    public void activate(Long id) {
        get(id).activate();
    }

    @Transactional
    public void deactivate(Long id) {
        get(id).deactivate();
    }

    private Coupon get(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));
    }
}
