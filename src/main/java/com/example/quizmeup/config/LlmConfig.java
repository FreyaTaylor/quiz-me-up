package com.example.quizmeup.config;


import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LlmConfig {

    @Value("${llm.api.url:https://api.deepseek.com/v1}")
    private String apiUrl;

    @Value("${llm.api.key:sk-5d0a2b34e8344844b5b4c9f73edf626d}")
    private String apiKey;

    @Value("${llm.api.model:deepseek-chat}")
    private String model;


    @Bean
    public ChatLanguageModel chatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(apiUrl)
                .apiKey(apiKey)
                .modelName(model)
                .maxTokens(8192)
                .temperature(0.7)
                .timeout(Duration.ofSeconds(240))
                .build();
    }
}
