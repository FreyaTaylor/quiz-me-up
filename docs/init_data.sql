-- ============================================
-- 初始化测试数据脚本
-- 用于 AI 面试平台预置数据
-- MySQL 8.0 兼容
-- ============================================

-- 1. 插入用户数据（明文密码）
INSERT INTO users (username, password, role) VALUES
('alice', '123456', NULL),
('bob', 'password', NULL),
('admin', 'admin123', 'ADMIN');

-- 2. 构建 Java 并发知识树
-- 根节点：Java
INSERT INTO lc_knowledge (id, parent_id, name, description, level, importance, is_leaf) VALUES
('java', NULL, 'Java', 'Java 编程语言知识体系', 1, 5, FALSE);

-- 中间节点：Java 并发
INSERT INTO lc_knowledge (id, parent_id, name, description, level, importance, is_leaf) VALUES
('java.concurrent', 'java', 'Java 并发', 'Java 并发编程相关知识', 2, 4, FALSE);

-- 叶节点：线程池
INSERT INTO lc_knowledge (id, parent_id, name, description, level, importance, is_leaf) VALUES
('java.concurrent.threadpool', 'java.concurrent', '线程池', 'ThreadPoolExecutor、线程池参数配置、拒绝策略等', 3, 4, TRUE);

-- 叶节点：锁机制
INSERT INTO lc_knowledge (id, parent_id, name, description, level, importance, is_leaf) VALUES
('java.concurrent.lock', 'java.concurrent', '锁机制', 'synchronized、ReentrantLock、CAS、AQS 等', 3, 5, TRUE);

-- 3. 插入 Prompt 模板
-- 生成面试题模板
INSERT INTO lc_prompt_template (code, content, description) VALUES
('INTERVIEW_Q_GEN', 
'你是一位资深的 Java 技术面试官。请根据以下知识点生成一道面试题：

知识点：{knowledge_name}
描述：{knowledge_description}

要求：
1. 题目应该考察该知识点的核心概念和实践应用
2. 难度适中，适合中级及以上开发者
3. 题目表述清晰，避免歧义
4. 如果涉及代码，请提供简洁的示例

请只输出题目内容，不要包含答案。',
'生成面试题');

-- 答案评分模板
INSERT INTO lc_prompt_template (code, content, description) VALUES
('ANSWER_EVALUATION',
'你是一位资深的 Java 技术面试官。请对以下面试答案进行评分。

题目：{question_text}
标准答案：{model_answer}
考生答案：{user_answer}

评分标准（0-100分）：
- 答案完整性（30分）：是否覆盖了题目的核心要点
- 技术准确性（40分）：技术描述是否正确，是否有错误概念
- 深度和广度（20分）：是否深入理解，是否提到相关扩展知识
- 表达清晰度（10分）：逻辑是否清晰，表述是否准确

请给出：
1. 总分（0-100）
2. 各维度得分
3. 简要评语和改进建议

格式：
总分：XX分
完整性：XX分
准确性：XX分
深度广度：XX分
表达清晰：XX分
评语：...',
'答案评分');

-- 知识树生成模板
INSERT INTO lc_prompt_template (code, content, description) VALUES
('KNOWLEDGE_TREE_GEN',
'你是一位资深的技术专家。请根据以下要求生成一个结构化的知识树。

知识树根节点：{knowledgeRoot}
建议节点数量：{count}

要求：
1. 生成一个层次化的知识树结构，包含根节点、中间节点和叶节点
2. 每个节点需要包含：id（唯一标识，使用点号分隔，如 "java.concurrent.threadpool"）、name（节点名称）、importance（重要性，1-5的整数）、children（子节点数组）
3. 叶节点（没有子节点的节点）的 children 应为空数组 []
4. 知识树应该覆盖该技术领域的核心知识点，层次清晰，逻辑合理
5. 节点数量尽量接近建议值，但可以适当调整以保证知识树的完整性

请严格按照以下 JSON 格式输出，只输出 JSON，不要包含其他文字说明：
{
  "data": {
    "id": "根节点id",
    "name": "根节点名称",
    "importance": "5",
    "children": [
      {
        "id": "子节点id",
        "name": "子节点名称",
        "importance": "4",
        "children": [
          {
            "id": "叶节点id",
            "name": "叶节点名称",
            "importance": "5",
            "children": []
          }
        ]
      }
    ]
  }
}',
'生成知识树');
