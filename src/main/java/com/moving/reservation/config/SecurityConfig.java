package com.moving.reservation.config;

import com.moving.reservation.auth.AdminAuthenticationFailureHandler;
import com.moving.reservation.auth.AdminAuthenticationSuccessHandler;
import com.moving.reservation.auth.AdminUserRepository;
import java.util.List;
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
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AdminAuthenticationFailureHandler adminAuthenticationFailureHandler,
                                                   AdminAuthenticationSuccessHandler adminAuthenticationSuccessHandler) throws Exception {
        http
                .cors(cors -> {
                })
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher("/reservations", "POST"),
                                new AntPathRequestMatcher("/api/reservations", "POST"),
                                new AntPathRequestMatcher("/api/reservations/*", "PATCH"),
                                new AntPathRequestMatcher("/api/reservations/*/cancel", "POST"),
                                new AntPathRequestMatcher("/api/reservations/*/estimate/accept", "POST"),
                                new AntPathRequestMatcher("/api/reservations/*/photos", "POST"),
                                new AntPathRequestMatcher("/api/reservations/search", "POST"),
                                new AntPathRequestMatcher("/api/reviews", "POST"),
                                new AntPathRequestMatcher("/api/admin/reservations/*/status", "PATCH"),
                                new AntPathRequestMatcher("/api/admin/reservations/*/estimate", "PATCH")
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/reservations").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reservations").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reservations/search").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reservations/*/cancel").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reservations/*/estimate/accept").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reservations/*/photos").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reviews").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/reservations/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/customer-guides/*").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/", "/login", "/faq", "/css/**", "/js/**", "/reservations", "/reservations/**",
                                "/reviews", "/reviews/**", "/uploads/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(adminAuthenticationSuccessHandler)
                        .failureHandler(adminAuthenticationFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionFixation(sessionFixation -> sessionFixation.migrateSession())
                        .invalidSessionUrl("/login?expired")
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?expired")
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/uploads/**", configuration);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService(AdminUserRepository adminUserRepository) {
        return username -> adminUserRepository.findByUsername(username)
                .map(adminUser -> User.builder()
                        .username(adminUser.getUsername())
                        .password(adminUser.getPassword())
                        .disabled(!adminUser.isEnabled())
                        .accountLocked(adminUser.isLoginLocked())
                        .roles(adminUser.getRole())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("관리자 계정을 찾을 수 없습니다."));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
