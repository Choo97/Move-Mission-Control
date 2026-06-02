package com.moving.reservation.coupon;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CouponCreateRequest {

    @NotBlank(message = "쿠폰 코드를 입력해 주세요.")
    @Pattern(regexp = "^[A-Z0-9_\\-]+$", message = "쿠폰 코드는 대문자, 숫자, 하이픈, 밑줄만 사용할 수 있습니다.")
    private String code;

    @NotBlank(message = "쿠폰 이름을 입력해 주세요.")
    private String name;

    @NotNull(message = "할인 방식을 선택해 주세요.")
    private DiscountType discountType;

    @NotNull(message = "할인 값을 입력해 주세요.")
    @Min(value = 1, message = "할인 값은 1 이상이어야 합니다.")
    @Max(value = 10000000, message = "할인 값이 너무 큽니다.")
    private Integer discountValue;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountType discountType) {
        this.discountType = discountType;
    }

    public Integer getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(Integer discountValue) {
        this.discountValue = discountValue;
    }
}
