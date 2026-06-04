package com.moving.reservation.faq;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FaqSaveRequest {

    @NotBlank(message = "질문을 입력해 주세요.")
    private String question;

    @NotBlank(message = "답변을 입력해 주세요.")
    private String answer;

    @NotNull(message = "정렬 순서를 입력해 주세요.")
    @Min(value = 1, message = "정렬 순서는 1 이상이어야 합니다.")
    @Max(value = 999, message = "정렬 순서가 너무 큽니다.")
    private Integer displayOrder;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
