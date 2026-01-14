package com.example.quizmeup.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiResponse<T> {
    private boolean success = true;
    private String message;
    private T data;

    // 构造函数、getter/setter（或用 Lombok）
    public static <T> AiResponse<T> success(T data) {
        AiResponse<T> res = new AiResponse<>();
        res.data = data;
        return res;
    }

    public static <T> AiResponse<T> error(String message) {
        AiResponse<T> res = new AiResponse<>();
        res.success = false;
        res.message = message;
        return res;
    }

}
