package com.example.quizmeup.ai;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;


public class LlmJsonParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 从 LLM 响应中提取并解析为指定类型的对象
     */
    public static <T> T parseLlmResponse(String llmResponse, Class<T> valueType) {
        if (llmResponse == null || llmResponse.trim().isEmpty()) {
            throw new IllegalArgumentException("LLM response is empty");
        }

        // Step 1: 清理 Markdown 代码块
        String cleaned = llmResponse.replaceAll("```(?:json|JSON)?", "").trim();

        // Step 2: 尝试直接解析（最理想情况）
        try {
            return objectMapper.readValue(cleaned, valueType);
        } catch (JsonProcessingException ignored) {
            // 继续尝试提取
        }

        // Step 3: 提取首 { 到尾 }
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start != -1 && end > start) {
            String jsonCandidate = cleaned.substring(start, end + 1);
            try {
                return objectMapper.readValue(jsonCandidate, valueType);
            } catch (JsonProcessingException ignored) {
                // ignore
            }
        }

        // Step 4: 尝试数组（虽然你不需要，但健壮性考虑）
        start = cleaned.indexOf('[');
        end = cleaned.lastIndexOf(']');
        if (start != -1 && end > start) {
            String jsonCandidate = cleaned.substring(start, end + 1);
            try {
                return objectMapper.readValue(jsonCandidate, valueType);
            } catch (JsonProcessingException ignored) {
                // ignore
            }
        }

        // 最终失败
        throw new IllegalArgumentException(
                "无法从 LLM 响应中解析出有效的 JSON 对象。原始响应: " + llmResponse
        );
    }
}


