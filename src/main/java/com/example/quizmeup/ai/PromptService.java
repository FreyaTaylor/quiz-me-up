package com.example.quizmeup.ai;

import com.example.quizmeup.entity.PromptTemplate;
import com.example.quizmeup.repository.PromptTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Prompt 模板服务实现类
 */
@Service
@RequiredArgsConstructor
public class PromptService {

    private final PromptTemplateRepository promptTemplateRepository;
    
    public String render(String templateCode, Map<String, Object> params) {
        // 1. 从数据库获取模板
        PromptTemplate template = promptTemplateRepository.findByCode(templateCode);

        if (template == null) {
            throw new RuntimeException("Prompt 模板不存在: " + templateCode);
        }

        // 2. 渲染模板（替换占位符）
        String content = template.getContent();
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                // 支持两种格式：{key} 和 {{key}}
                String placeholder1 = "{" + entry.getKey() + "}";
                String placeholder2 = "{{" + entry.getKey() + "}}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                content = content.replace(placeholder1, value);
                content = content.replace(placeholder2, value);
            }
        }

        return content;
    }
}
