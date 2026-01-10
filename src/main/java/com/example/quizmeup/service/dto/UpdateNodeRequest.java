package com.example.quizmeup.service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新节点请求DTO。
 */
@Data
public class UpdateNodeRequest {
    @NotBlank(message = "节点ID不能为空")
    private String nodeId;
    
    private String name;
    
    private String description;
    
    @Min(value = 1, message = "重要程度必须在1-5之间")
    @Max(value = 5, message = "重要程度必须在1-5之间")
    private Integer importance;
}
