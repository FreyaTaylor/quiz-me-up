package com.example.quizmeup.ai;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.quizmeup.domain.entity.PromptTemplate;
import com.example.quizmeup.infra.mapper.PromptTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Prompt 管理，支持简单占位符替换。
 */
@Component
@RequiredArgsConstructor
public class PromptService {

    private final PromptTemplateMapper promptTemplateMapper;

    public String render(String code, Map<String, Object> vars) {
        PromptTemplate tpl = promptTemplateMapper.selectOne(
                Wrappers.<PromptTemplate>lambdaQuery().eq(PromptTemplate::getCode, code));
        if (tpl == null) {
            throw new IllegalArgumentException("Prompt missing: " + code);
        }
        String jsonVars = JSON.toJSONString(vars);
        return tpl.getContent().replace("{{vars}}", jsonVars);
    }
}
