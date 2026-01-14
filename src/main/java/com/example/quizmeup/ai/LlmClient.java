package com.example.quizmeup.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * LLM 客户端实现类
 * 使用 HTTP 调用 OpenAI API（或其他兼容的 LLM API）
 */
@Service
@RequiredArgsConstructor
public class LlmClient {


    private final ChatLanguageModel chatModel;


    public String call(String prompt) {
        return chatModel.generate(prompt);
    }
}
