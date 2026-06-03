package com.moving.reservation.estimate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/estimate-settings")
public class EstimateSettingController {

    private final EstimateSettingService estimateSettingService;

    public EstimateSettingController(EstimateSettingService estimateSettingService) {
        this.estimateSettingService = estimateSettingService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("settings", estimateSettingService.findAll());
        return "admin/estimate-settings";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam int amount,
                         RedirectAttributes redirectAttributes) {
        if (amount < 0) {
            redirectAttributes.addFlashAttribute("estimateSettingError", "견적 기준 금액은 0 이상이어야 합니다.");
            return "redirect:/admin/estimate-settings";
        }

        estimateSettingService.update(id, amount);
        redirectAttributes.addFlashAttribute("estimateSettingMessage", "견적 기준이 수정되었습니다.");
        return "redirect:/admin/estimate-settings";
    }
}
