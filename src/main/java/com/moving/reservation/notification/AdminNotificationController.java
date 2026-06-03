package com.moving.reservation.notification;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/notifications")
public class AdminNotificationController {

    private final CustomerNotificationService customerNotificationService;

    public AdminNotificationController(CustomerNotificationService customerNotificationService) {
        this.customerNotificationService = customerNotificationService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) NotificationChannel channel,
                       @RequestParam(required = false) NotificationStatus status,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        model.addAttribute("notifications", customerNotificationService.search(channel, status, keyword));
        model.addAttribute("channels", NotificationChannel.values());
        model.addAttribute("statuses", NotificationStatus.values());
        model.addAttribute("selectedChannel", channel);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        return "admin/notifications";
    }
}
