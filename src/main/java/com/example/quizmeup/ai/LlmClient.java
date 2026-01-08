package com.example.quizmeup.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 对 LangChain4j ChatModel 的简单封装。
 */
@Component
@RequiredArgsConstructor
public class LlmClient {

    private final ChatLanguageModel chatModel;

    public String call(String prompt) {
        return chatModel.generate(prompt);
    }
}
