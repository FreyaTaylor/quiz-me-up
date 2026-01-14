package com.example.quizmeup.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.quizmeup.entity.PromptTemplate;
import com.example.quizmeup.mapper.PromptTemplateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Prompt 模板服务实现类
 */
@Service
public class PromptService {

    @Autowired
    private PromptTemplateMapper promptTemplateMapper;
    
    public String render(String templateCode, Map<String, Object> params) {
        // 1. 从数据库获取模板
        LambdaQueryWrapper<PromptTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptTemplate::getCode, templateCode);
        PromptTemplate template = promptTemplateMapper.selectOne(wrapper);

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
