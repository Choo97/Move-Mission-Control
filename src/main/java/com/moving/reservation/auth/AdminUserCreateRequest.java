package com.moving.reservation.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AdminUserCreateRequest {

    @NotBlank(message = "아이디를 입력해 주세요.")
    @Size(min = 4, max = 50, message = "아이디는 4자 이상 50자 이하로 입력해 주세요.")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "아이디는 영문, 숫자, 밑줄만 사용할 수 있습니다.")
    private String username;

    @NotBlank(message = "초기 비밀번호를 입력해 주세요.")
    @Size(min = 8, message = "초기 비밀번호는 8자 이상 입력해 주세요.")
    private String password;

    @NotBlank(message = "초기 비밀번호 확인을 입력해 주세요.")
    private String confirmPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
