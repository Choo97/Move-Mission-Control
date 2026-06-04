package com.moving.reservation.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminLoginAttemptService {

    private final AdminUserRepository adminUserRepository;
    private final int maxFailureCount;
    private final int lockMinutes;

    public AdminLoginAttemptService(AdminUserRepository adminUserRepository,
                                    @Value("${admin.security.login.max-failure-count}") int maxFailureCount,
                                    @Value("${admin.security.login.lock-minutes}") int lockMinutes) {
        this.adminUserRepository = adminUserRepository;
        this.maxFailureCount = maxFailureCount;
        this.lockMinutes = lockMinutes;
    }

    @Transactional
    public void recordFailure(String username) {
        if (username == null || username.isBlank()) {
            return;
        }

        adminUserRepository.findByUsername(username.trim())
                .ifPresent(adminUser -> adminUser.recordLoginFailure(maxFailureCount, lockMinutes));
    }

    @Transactional
    public void resetFailures(String username) {
        if (username == null || username.isBlank()) {
            return;
        }

        adminUserRepository.findByUsername(username.trim())
                .ifPresent(AdminUser::resetLoginFailures);
    }

    public int getMaxFailureCount() {
        return maxFailureCount;
    }

    public int getLockMinutes() {
        return lockMinutes;
    }
}
