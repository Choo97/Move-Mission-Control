package com.moving.reservation.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin1234";

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminUserRepository.existsByUsername(DEFAULT_USERNAME)) {
            return;
        }

        adminUserRepository.save(new AdminUser(
                DEFAULT_USERNAME,
                passwordEncoder.encode(DEFAULT_PASSWORD),
                "ADMIN"
        ));
    }
}
