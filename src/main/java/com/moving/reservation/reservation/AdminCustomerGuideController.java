package com.moving.reservation.reservation;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/customer-guides")
public class AdminCustomerGuideController {

    private final CustomerGuideService customerGuideService;

    public AdminCustomerGuideController(CustomerGuideService customerGuideService) {
        this.customerGuideService = customerGuideService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("customerGuides", customerGuideService.findAll());
        model.addAttribute("customerGuideSaveRequest", new CustomerGuideSaveRequest());
        model.addAttribute("statuses", ReservationStatus.values());
        return "admin/customer-guides";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute CustomerGuideSaveRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("customerGuides", customerGuideService.findAll());
            model.addAttribute("statuses", ReservationStatus.values());
            return "admin/customer-guides";
        }

        customerGuideService.create(request);
        redirectAttributes.addFlashAttribute("customerGuideMessage", "고객 안내가 추가되었습니다.");
        return "redirect:/admin/customer-guides";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute CustomerGuideSaveRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("customerGuideError", "상태, 제목, 설명, 정렬 순서를 올바르게 입력해 주세요.");
            return "redirect:/admin/customer-guides";
        }

        customerGuideService.update(id, request);
        redirectAttributes.addFlashAttribute("customerGuideMessage", "고객 안내가 수정되었습니다.");
        return "redirect:/admin/customer-guides";
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        customerGuideService.activate(id);
        redirectAttributes.addFlashAttribute("customerGuideMessage", "고객 안내가 공개되었습니다.");
        return "redirect:/admin/customer-guides";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        customerGuideService.deactivate(id);
        redirectAttributes.addFlashAttribute("customerGuideMessage", "고객 안내가 숨김 처리되었습니다.");
        return "redirect:/admin/customer-guides";
    }
}
