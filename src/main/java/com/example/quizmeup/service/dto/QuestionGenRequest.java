package com.example.quizmeup.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionGenRequest {
    @NotNull
    private Long userId;
    @NotBlank
    private String topic;
}
