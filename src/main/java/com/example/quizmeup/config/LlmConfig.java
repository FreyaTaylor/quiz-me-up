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
    public ChatLanguageModel chatModel() {
        return OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com/v1") // 👈 关键：改 base URL
                .apiKey("sk-5d0a2b34e8344844b5b4c9f73edf626d")     // 👈 替换为你的 Key
                .modelName("deepseek-chat")             // 👈 模型名固定为此
                .maxTokens(1024)
                .temperature(0.7)
                .build();
    }
}
