package com.example.quizmeup.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnswerRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Long questionId;
    @NotBlank
    private String answer;
}
