package com.moving.reservation.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class AdminAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final AdminLoginAttemptService adminLoginAttemptService;

    public AdminAuthenticationFailureHandler(AdminLoginAttemptService adminLoginAttemptService) {
        super("/login?error");
        this.adminLoginAttemptService = adminLoginAttemptService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        adminLoginAttemptService.recordFailure(request.getParameter("username"));
        super.onAuthenticationFailure(request, response, exception);
    }
}
