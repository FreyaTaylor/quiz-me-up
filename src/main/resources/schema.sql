-- ============================================
-- 数据库表结构定义
-- ============================================

-- 知识点表（树形结构）
CREATE TABLE IF NOT EXISTS lc_knowledge (
    id VARCHAR(128) PRIMARY KEY,
    parent_id VARCHAR(128) DEFAULT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(512),
    level INT DEFAULT 1,
    path VARCHAR(512),
    importance TINYINT DEFAULT 3 COMMENT '重要性 1-5',
    is_leaf BOOLEAN DEFAULT FALSE COMMENT '是否为叶节点',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_is_leaf (is_leaf)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 题目表（关联知识点叶节点）
CREATE TABLE IF NOT EXISTS lc_questions (
    id VARCHAR(128) PRIMARY KEY,
    knowledge_id VARCHAR(128) NOT NULL,
    question_text TEXT NOT NULL,
    model_answer TEXT NOT NULL,
    difficulty TINYINT DEFAULT 3 COMMENT '难度 1-5',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_knowledge_id (knowledge_id),
    FOREIGN KEY (knowledge_id) REFERENCES lc_knowledge(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户答题记录
CREATE TABLE IF NOT EXISTS lc_question_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    question_id VARCHAR(128) NOT NULL,
    score TINYINT DEFAULT 0 COMMENT '得分 0-100',
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_question_id (question_id),
    INDEX idx_submitted_at (submitted_at),
    FOREIGN KEY (question_id) REFERENCES lc_questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户知识点掌握度（按knowledge_id聚合）
CREATE TABLE IF NOT EXISTS lc_user_mastery (
    user_id BIGINT NOT NULL,
    knowledge_id VARCHAR(128) NOT NULL,
    proficiency TINYINT NOT NULL DEFAULT 0 COMMENT '该知识点下所有题的平均分 0-100',
    total_questions INT NOT NULL DEFAULT 0 COMMENT '该知识点总题数',
    practiced_count INT NOT NULL DEFAULT 0 COMMENT '已答题数',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, knowledge_id),
    FOREIGN KEY (knowledge_id) REFERENCES lc_knowledge(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Prompt模板表
CREATE TABLE IF NOT EXISTS lc_prompt_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL UNIQUE,
    content TEXT NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
