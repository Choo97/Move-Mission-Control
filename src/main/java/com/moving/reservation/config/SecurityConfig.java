package com.moving.reservation.config;

import com.moving.reservation.auth.AdminUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(new AntPathRequestMatcher("/reservations", "POST"))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/reservations").permitAll()
                        .requestMatchers("/", "/login", "/faq", "/css/**", "/js/**", "/reservations", "/reservations/**",
                                "/reviews", "/reviews/**").permitAll()
                        .requestMatchers("/admin/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/admin/reservations", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(AdminUserRepository adminUserRepository) {
        return username -> adminUserRepository.findByUsername(username)
                .map(adminUser -> User.builder()
                        .username(adminUser.getUsername())
                        .password(adminUser.getPassword())
                        .disabled(!adminUser.isEnabled())
                        .roles(adminUser.getRole())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("관리자 계정을 찾을 수 없습니다."));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
