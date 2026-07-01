package com.moving.reservation.privacy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PersonalInfoMaskerTest {

    @Test
    void 이름은_첫글자만_남긴다() {
        assertThat(PersonalInfoMasker.maskName("김민수")).isEqualTo("김**");
        assertThat(PersonalInfoMasker.maskName("React고객")).isEqualTo("R**");
    }

    @Test
    void 전화번호는_마지막_네자리만_남긴다() {
        assertThat(PersonalInfoMasker.maskPhone("010-1234-5678")).isEqualTo("010-****-5678");
        assertThat(PersonalInfoMasker.maskPhone("0212345678")).isEqualTo("021-****-5678");
    }

    @Test
    void 이메일은_계정_첫글자와_도메인만_남긴다() {
        assertThat(PersonalInfoMasker.maskEmail("customer@example.com")).isEqualTo("c***@example.com");
        assertThat(PersonalInfoMasker.maskEmail(null)).isNull();
    }

    @Test
    void 주소는_시군구_수준까지만_남긴다() {
        assertThat(PersonalInfoMasker.maskAddress("서울시 강남구 테헤란로 1")).isEqualTo("서울시 강남구 ***");
        assertThat(PersonalInfoMasker.maskAddress("제주도")).isEqualTo("제주도 ***");
    }

    @Test
    void 연락처는_이메일과_전화번호를_구분해_마스킹한다() {
        assertThat(PersonalInfoMasker.maskContact("admin@example.com")).isEqualTo("a***@example.com");
        assertThat(PersonalInfoMasker.maskContact("010-1111-2222")).isEqualTo("010-****-2222");
    }
}
