package com.moving.reservation.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String initialUsername;
    private final String initialPassword;
    private final String initialRole;

    public AdminUserInitializer(AdminUserRepository adminUserRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${admin.initial.username}") String initialUsername,
                                @Value("${admin.initial.password}") String initialPassword,
                                @Value("${admin.initial.role}") String initialRole) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.initialUsername = initialUsername;
        this.initialPassword = initialPassword;
        this.initialRole = initialRole;
    }

    @Override
    public void run(String... args) {
        String username = initialUsername.trim();
        String password = initialPassword.trim();
        String role = initialRole.trim();

        if (username.isBlank() || password.isBlank() || role.isBlank()) {
            throw new IllegalStateException("초기 관리자 계정 설정은 비워둘 수 없습니다.");
        }

        if (adminUserRepository.existsByUsername(username)) {
            return;
        }

        adminUserRepository.save(new AdminUser(
                username,
                passwordEncoder.encode(password),
                role
        ));
    }
}
