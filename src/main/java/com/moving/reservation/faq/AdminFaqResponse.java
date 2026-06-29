package com.moving.reservation.faq;

import java.time.LocalDateTime;

public record AdminFaqResponse(
        Long id,
        String question,
        String answer,
        Integer displayOrder,
        boolean active,
        LocalDateTime createdAt
) {

    public static AdminFaqResponse from(Faq faq) {
        return new AdminFaqResponse(
                faq.getId(),
                faq.getQuestion(),
                faq.getAnswer(),
                faq.getDisplayOrder(),
                faq.isActive(),
                faq.getCreatedAt()
        );
    }
}
