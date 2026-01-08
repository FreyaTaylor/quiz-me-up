-- 初始化 Prompt 模板数据
INSERT INTO lc_prompt_template (code, content, description) VALUES
('QUESTION_GEN', '你是技术面试官，请基于主题 {{vars}} 生成10道面试题，返回JSON数组格式：[{"knowledgeId": 1, "question": "问题内容"}]. 确保问题有针对性且实用。', '生成面试题的Prompt模板'),
('ANSWER_REVIEW', '你是技术面试官，请对以下答案进行评分（0-100分）并给出分析，返回JSON格式：{"score": 85, "analysis": "分析内容"}. 问题：{{vars}}', '答案评分的Prompt模板');

-- 初始化示例知识点数据（MySQL相关）
INSERT INTO lc_knowledge (parent_id, name, description, level) VALUES
(0, 'MySQL基础', 'MySQL数据库基础知识', 1),
(0, 'Java基础', 'Java编程语言基础知识', 1),
(1, 'MySQL索引', 'MySQL索引原理和使用', 2),
(1, 'MySQL事务', 'MySQL事务ACID特性', 2),
(2, 'Java集合', 'Java集合框架', 2),
(2, 'Java多线程', 'Java并发编程', 2);
