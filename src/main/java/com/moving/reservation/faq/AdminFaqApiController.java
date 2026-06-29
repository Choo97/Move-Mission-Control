package com.moving.reservation.faq;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/faqs")
public class AdminFaqApiController {

    private final FaqService faqService;

    public AdminFaqApiController(FaqService faqService) {
        this.faqService = faqService;
    }

    @GetMapping
    public List<AdminFaqResponse> list() {
        return faqService.findAll()
                .stream()
                .map(AdminFaqResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminFaqResponse create(@RequestBody FaqSaveRequest request) {
        return AdminFaqResponse.from(faqService.create(request));
    }

    @PatchMapping("/{id}")
    public AdminFaqResponse update(@PathVariable Long id, @RequestBody FaqSaveRequest request) {
        return AdminFaqResponse.from(faqService.update(id, request));
    }

    @PatchMapping("/{id}/active")
    public AdminFaqResponse updateActive(@PathVariable Long id, @RequestBody AdminFaqActiveRequest request) {
        Faq faq = request.active() ? faqService.activate(id) : faqService.deactivate(id);
        return AdminFaqResponse.from(faq);
    }
}
