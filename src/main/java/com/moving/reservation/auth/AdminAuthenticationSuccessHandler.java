package com.moving.reservation.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class AdminAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final AdminLoginAttemptService adminLoginAttemptService;

    public AdminAuthenticationSuccessHandler(AdminLoginAttemptService adminLoginAttemptService) {
        this.adminLoginAttemptService = adminLoginAttemptService;
        setDefaultTargetUrl("/admin/reservations");
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        adminLoginAttemptService.resetFailures(authentication.getName());
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
