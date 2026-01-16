package com.example.quizmeup.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LlmConfig {

    // 默认模型（出题、评分等）
    @Value("${llm.api.url:https://api.deepseek.com/v1}")
    private String apiUrl;

    @Value("${llm.api.key:sk-5d0a2b34e8344844b5b4c9f73edf626d}")
    private String apiKey;

    @Value("${llm.api.model:deepseek-chat}")
    private String model;

    @Value("${llm.tree.api.treeModel:deepseek-reasoner}")
    private String reasonerModel;

    /**
     * 默认 Chat 模型：用于出题、评分等学习流程
     */
    @Bean
    public ChatLanguageModel chatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(apiUrl)
                .apiKey(apiKey)
                .modelName(model)
                .maxTokens(2048)
                .temperature(0.7)
                .timeout(Duration.ofSeconds(120))
                .build();
    }

    /**
     * 知识树初始化专用 Chat 模型：用于 /knowledge/init
     */
    @Bean
    public ChatLanguageModel reasonerModel() {
        return OpenAiChatModel.builder()
                .baseUrl(apiUrl)
                .apiKey(apiKey)
                .modelName(reasonerModel)
                .maxTokens(8192)
                .temperature(0.3) // 通常结构化任务温度可以低一些
                .timeout(Duration.ofSeconds(300))
                .build();
    }
}
