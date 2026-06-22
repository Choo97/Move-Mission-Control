package com.moving.reservation.faq;

public record FaqResponse(Long id, String question, String answer) {
    public static FaqResponse from(Faq faq) {
        return new FaqResponse(faq.getId(), faq.getQuestion(), faq.getAnswer());
    }
}
