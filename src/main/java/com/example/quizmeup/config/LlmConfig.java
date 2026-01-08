package com.example.quizmeup.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j OpenAI 模型配置。
 */
@Configuration
public class LlmConfig {

    @Bean
    public ChatLanguageModel chatLanguageModel(
            @Value("${langchain4j.openai.api-key:dummy}") String apiKey,
            @Value("${langchain4j.openai.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${langchain4j.openai.model-name:gpt-4o-mini}") String modelName) {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .build();
    }
}
