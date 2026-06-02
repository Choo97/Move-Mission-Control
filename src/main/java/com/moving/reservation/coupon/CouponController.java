package com.moving.reservation.coupon;

import com.moving.reservation.reservation.ReservationService;
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
@RequestMapping("/admin/coupons")
public class CouponController {

    private final CouponService couponService;
    private final ReservationService reservationService;

    public CouponController(CouponService couponService, ReservationService reservationService) {
        this.couponService = couponService;
        this.reservationService = reservationService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("coupons", couponService.findAll());
        model.addAttribute("couponCreateRequest", new CouponCreateRequest());
        model.addAttribute("discountTypes", DiscountType.values());
        return "admin/coupons";
    }

    @GetMapping("/usages")
    public String usages(Model model) {
        model.addAttribute("couponUsages", reservationService.findCouponUsages());
        return "admin/coupon-usages";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute CouponCreateRequest request,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("coupons", couponService.findAll());
            model.addAttribute("discountTypes", DiscountType.values());
            return "admin/coupons";
        }

        try {
            couponService.create(request);
            return "redirect:/admin/coupons";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("coupons", couponService.findAll());
            model.addAttribute("discountTypes", DiscountType.values());
            model.addAttribute("couponError", exception.getMessage());
            return "admin/coupons";
        }
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        couponService.activate(id);
        redirectAttributes.addFlashAttribute("couponMessage", "쿠폰이 활성화되었습니다.");
        return "redirect:/admin/coupons";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        couponService.deactivate(id);
        redirectAttributes.addFlashAttribute("couponMessage", "쿠폰이 비활성화되었습니다.");
        return "redirect:/admin/coupons";
    }
}
