package com.moving.reservation.admin;

import com.moving.reservation.notification.CustomerNotificationService;
import com.moving.reservation.notification.NotificationChannel;
import com.moving.reservation.notification.NotificationStatus;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/notifications")
public class AdminNotificationApiController {

    private final CustomerNotificationService customerNotificationService;

    public AdminNotificationApiController(CustomerNotificationService customerNotificationService) {
        this.customerNotificationService = customerNotificationService;
    }

    @GetMapping
    public List<AdminNotificationListItemResponse> list(@RequestParam(required = false) NotificationChannel channel,
                                                        @RequestParam(required = false) NotificationStatus status,
                                                        @RequestParam(required = false) String keyword) {
        return customerNotificationService.search(channel, status, keyword)
                .stream()
                .map(AdminNotificationListItemResponse::from)
                .toList();
    }

    @GetMapping("/action-items")
    public List<AdminNotificationActionItemResponse> actionItems(@RequestParam(defaultValue = "8") int limit) {
        return customerNotificationService.findActionRequired(limit)
                .stream()
                .map(AdminNotificationActionItemResponse::from)
                .toList();
    }
}
