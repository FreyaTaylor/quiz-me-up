CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL,
    role VARCHAR(16) DEFAULT NULL COMMENT '用户角色，ADMIN为管理员，NULL为普通用户',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE lc_knowledge (
    id VARCHAR(128) PRIMARY KEY, -- "java.concurrent.threadpool"
    parent_id VARCHAR(128) DEFAULT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(512),
    level TINYINT DEFAULT 1,
    importance TINYINT DEFAULT 3 CHECK (importance BETWEEN 1 AND 5),
    is_leaf BOOLEAN DEFAULT FALSE COMMENT '是否为叶节点',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_is_leaf (is_leaf)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE lc_questions (
    id VARCHAR(128) PRIMARY KEY,
    knowledge_id VARCHAR(128) NOT NULL,
    question_text VARCHAR(1000) NOT NULL,
    model_answer VARCHAR(2000) NOT NULL,
    importance TINYINT DEFAULT 3 CHECK (importance BETWEEN 1 AND 5),
    difficulty TINYINT DEFAULT 3 CHECK (difficulty BETWEEN 1 AND 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_knowledge_id (knowledge_id),
    FOREIGN KEY (knowledge_id) REFERENCES lc_knowledge(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE lc_question_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    question_id VARCHAR(128) NOT NULL,
    score DECIMAL(4,1) DEFAULT 0.0 COMMENT '得分 0.0–100.0，保留1位小数',
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_question_id (question_id),
    INDEX idx_submitted_at (submitted_at),
    FOREIGN KEY (question_id) REFERENCES lc_questions(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE lc_user_mastery (
    user_id BIGINT NOT NULL,
    knowledge_id VARCHAR(128) NOT NULL,
    proficiency DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '平均分（0.00～100.00）',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, knowledge_id),
    FOREIGN KEY (knowledge_id) REFERENCES lc_knowledge(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE lc_prompt_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL UNIQUE,
    content TEXT NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;