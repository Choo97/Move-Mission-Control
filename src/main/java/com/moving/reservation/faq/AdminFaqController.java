package com.moving.reservation.faq;

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
@RequestMapping("/admin/faqs")
public class AdminFaqController {

    private final FaqService faqService;

    public AdminFaqController(FaqService faqService) {
        this.faqService = faqService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("faqs", faqService.findAll());
        model.addAttribute("faqSaveRequest", new FaqSaveRequest());
        return "admin/faqs";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute FaqSaveRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("faqs", faqService.findAll());
            return "admin/faqs";
        }

        faqService.create(request);
        redirectAttributes.addFlashAttribute("faqMessage", "FAQ가 추가되었습니다.");
        return "redirect:/admin/faqs";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute FaqSaveRequest request,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("faqError", "질문, 답변, 정렬 순서를 모두 올바르게 입력해 주세요.");
            return "redirect:/admin/faqs";
        }

        faqService.update(id, request);
        redirectAttributes.addFlashAttribute("faqMessage", "FAQ가 수정되었습니다.");
        return "redirect:/admin/faqs";
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        faqService.activate(id);
        redirectAttributes.addFlashAttribute("faqMessage", "FAQ가 공개되었습니다.");
        return "redirect:/admin/faqs";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        faqService.deactivate(id);
        redirectAttributes.addFlashAttribute("faqMessage", "FAQ가 숨김 처리되었습니다.");
        return "redirect:/admin/faqs";
    }
}
