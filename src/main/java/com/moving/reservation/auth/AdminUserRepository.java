package com.moving.reservation.auth;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    List<AdminUser> findAllByOrderByUsernameAsc();

    Optional<AdminUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
