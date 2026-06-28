package com.moving.reservation.auth;

import com.moving.reservation.api.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/session")
public class AdminSessionApiController {

    private final AuthenticationManager authenticationManager;
    private final AdminLoginAttemptService adminLoginAttemptService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AdminSessionApiController(AuthenticationManager authenticationManager,
                                     AdminLoginAttemptService adminLoginAttemptService) {
        this.authenticationManager = authenticationManager;
        this.adminLoginAttemptService = adminLoginAttemptService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody(required = false) AdminLoginApiRequest request,
                                   HttpServletRequest httpServletRequest,
                                   HttpServletResponse httpServletResponse) {
        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            return ResponseEntity.badRequest()
                    .body(ApiErrorResponse.badRequest("아이디와 비밀번호를 입력해 주세요."));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.username().trim(),
                            request.password()
                    )
            );
            adminLoginAttemptService.resetFailures(authentication.getName());
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            securityContextRepository.saveContext(securityContext, httpServletRequest, httpServletResponse);
            return ResponseEntity.ok(new AdminLoginApiResponse(authentication.getName()));
        } catch (AuthenticationException exception) {
            adminLoginAttemptService.recordFailure(request.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiErrorResponse.unauthorized("아이디 또는 비밀번호가 올바르지 않습니다."));
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
