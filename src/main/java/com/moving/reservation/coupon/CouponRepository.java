package com.moving.reservation.coupon;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    List<Coupon> findAllByOrderByCreatedAtDesc();

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);
}
