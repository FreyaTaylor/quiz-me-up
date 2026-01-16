package com.example.quizmeup.common;

/**
 * 系统常量类
 * 统一管理项目中使用的常量值
 */
public final class Constants {

    private Constants() {
        // 工具类，禁止实例化
    }

    /**
     * 默认难度值（1-5 范围）
     */
    public static final int DEFAULT_DIFFICULTY = 3;

    /**
     * 默认重要性值（1-5 范围）
     */
    public static final int DEFAULT_IMPORTANCE = 3;

    /**
     * 默认层级值（从 1 开始）
     */
    public static final int DEFAULT_LEVEL = 1;

    /**
     * 根节点标识符（用于构建树结构）
     */
    public static final String ROOT_NODE_ID = "ROOT";

    /**
     * 题目ID分隔符
     */
    public static final String QUESTION_ID_SEPARATOR = "_Q";

    /**
     * 知识点ID分隔符
     */
    public static final String KNOWLEDGE_ID_SEPARATOR = ".";

    /**
     * HTTP 状态码 - 成功
     */
    public static final int HTTP_STATUS_SUCCESS = 200;

    /**
     * HTTP 状态码 - 客户端错误
     */
    public static final int HTTP_STATUS_BAD_REQUEST = 400;

    /**
     * HTTP 状态码 - 未授权
     */
    public static final int HTTP_STATUS_UNAUTHORIZED = 401;
}
