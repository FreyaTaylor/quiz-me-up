package com.example.quizmeup.service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建根节点请求DTO。
 */
@Data
public class CreateRootNodeRequest {
    @NotBlank(message = "节点名称不能为空")
    private String name;
    
    private String description;
    
    @Min(value = 1, message = "重要程度必须在1-5之间")
    @Max(value = 5, message = "重要程度必须在1-5之间")
    private Integer importance = 3;
}
