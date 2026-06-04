package com.moving.reservation.auth;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminAccountService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String unsafeDefaultPassword;

    public AdminAccountService(AdminUserRepository adminUserRepository,
                               PasswordEncoder passwordEncoder,
                               @Value("${admin.security.unsafe-default-password}") String unsafeDefaultPassword) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.unsafeDefaultPassword = unsafeDefaultPassword;
    }

    public List<AdminUser> findAll() {
        return adminUserRepository.findAllByOrderByUsernameAsc();
    }

    public boolean usesUnsafeDefaultPassword(String username) {
        return adminUserRepository.findByUsername(username)
                .map(adminUser -> passwordEncoder.matches(unsafeDefaultPassword, adminUser.getPassword()))
                .orElse(false);
    }

    @Transactional
    public void create(AdminUserCreateRequest request) {
        String username = request.getUsername().trim();

        if (adminUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 사용 중인 관리자 아이디입니다.");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("초기 비밀번호와 확인 값이 일치하지 않습니다.");
        }

        adminUserRepository.save(new AdminUser(
                username,
                passwordEncoder.encode(request.getPassword()),
                "ADMIN"
        ));
    }

    @Transactional
    public void changePassword(String username, AdminPasswordChangeRequest request) {
        AdminUser adminUser = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("관리자 계정을 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), adminUser.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("새 비밀번호와 확인 값이 일치하지 않습니다.");
        }

        adminUser.changePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Transactional
    public void activate(Long id) {
        AdminUser adminUser = getAdminUser(id);
        adminUser.activate();
    }

    @Transactional
    public void deactivate(Long id, String currentUsername) {
        AdminUser adminUser = getAdminUser(id);

        if (adminUser.getUsername().equals(currentUsername)) {
            throw new IllegalArgumentException("현재 로그인한 계정은 비활성화할 수 없습니다.");
        }

        adminUser.deactivate();
    }

    private AdminUser getAdminUser(Long id) {
        return adminUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("관리자 계정을 찾을 수 없습니다."));
    }
}
