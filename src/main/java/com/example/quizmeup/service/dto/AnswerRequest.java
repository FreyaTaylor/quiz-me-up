package com.example.quizmeup.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnswerRequest {
    @NotNull
    private Long userId;
    @NotBlank
    private String questionId; // 改为String类型
    @NotBlank
    private String answer;
}
