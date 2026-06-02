package com.moving.reservation.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/account")
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    public AdminAccountController(AdminAccountService adminAccountService) {
        this.adminAccountService = adminAccountService;
    }

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        model.addAttribute("adminUserCreateRequest", new AdminUserCreateRequest());
        return "auth/user-new";
    }

    @PostMapping("/users")
    public String createUser(@Valid @ModelAttribute AdminUserCreateRequest request,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/user-new";
        }

        try {
            adminAccountService.create(request);
            model.addAttribute("createMessage", "관리자 계정이 생성되었습니다.");
            model.addAttribute("adminUserCreateRequest", new AdminUserCreateRequest());
            return "auth/user-new";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("createError", exception.getMessage());
            return "auth/user-new";
        }
    }

    @GetMapping("/password")
    public String passwordForm(Model model) {
        model.addAttribute("adminPasswordChangeRequest", new AdminPasswordChangeRequest());
        return "auth/password";
    }

    @PostMapping("/password")
    public String changePassword(@Valid @ModelAttribute AdminPasswordChangeRequest request,
                                 BindingResult bindingResult,
                                 Principal principal,
                                 HttpServletRequest httpServletRequest,
                                 HttpServletResponse httpServletResponse,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/password";
        }

        try {
            adminAccountService.changePassword(principal.getName(), request);
            new SecurityContextLogoutHandler().logout(httpServletRequest, httpServletResponse, null);
            return "redirect:/login?passwordChanged";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("changeError", exception.getMessage());
            return "auth/password";
        }
    }
}
