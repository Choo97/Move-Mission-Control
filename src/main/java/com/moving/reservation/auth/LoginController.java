package com.moving.reservation.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    private final AdminLoginAttemptService adminLoginAttemptService;

    public LoginController(AdminLoginAttemptService adminLoginAttemptService) {
        this.adminLoginAttemptService = adminLoginAttemptService;
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("maxFailureCount", adminLoginAttemptService.getMaxFailureCount());
        model.addAttribute("lockMinutes", adminLoginAttemptService.getLockMinutes());
        return "auth/login";
    }
}
