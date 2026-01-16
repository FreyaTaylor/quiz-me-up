package com.example.quizmeup.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 专门用于知识树初始化的 LLM 客户端
 * 使用独立的模型配置，可与默认模型不同
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReasonerLlmClient {

    /**
     * 知识树初始化专用模型
     */
    private final @Qualifier("reasonerModel") ChatLanguageModel reasonerModel;

    public String call(String prompt) {
        log.info(prompt);
        String resp = reasonerModel.generate(prompt);
        log.info(resp);
        return resp;
    }
}