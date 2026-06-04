package com.moving.reservation.admin;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/audit-logs")
public class AdminAuditLogController {

    private final AdminAuditLogService adminAuditLogService;

    public AdminAuditLogController(AdminAuditLogService adminAuditLogService) {
        this.adminAuditLogService = adminAuditLogService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String action,
                       @RequestParam(required = false) String createdBy,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                       Model model) {
        model.addAttribute("auditLogs", adminAuditLogService.search(action, createdBy, keyword, startDate, endDate));
        model.addAttribute("actions", adminAuditLogService.findActions());
        model.addAttribute("selectedAction", action);
        model.addAttribute("createdBy", createdBy);
        model.addAttribute("keyword", keyword);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "admin/audit-logs";
    }
}
