package com.example.quizmeup.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * AI扩充子树请求DTO。
 */
@Data
public class ExpandSubtreeRequest {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotBlank(message = "节点ID不能为空")
    private String nodeId;
}
