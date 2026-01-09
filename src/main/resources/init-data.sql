-- ============================================
-- 初始化数据 SQL 脚本
-- ============================================

-- 1. 初始化 Prompt 模板数据
INSERT INTO lc_prompt_template (code, content, description) VALUES
('QUESTION_GEN', '你是技术面试官，请基于主题 {{vars}} 生成10道面试题，返回JSON数组格式：[{"knowledgeId": 1, "question": "问题内容"}]. 确保问题有针对性且实用。', '生成面试题的Prompt模板'),
('ANSWER_REVIEW', '你是技术面试官，请对以下答案进行详细评估。首先分析问题的关键评价标准（通常4-6个，如：核心定义、关键特性、使用场景/注意事项、常见误区等），然后对用户答案进行评分和分析。返回JSON格式：{"score": 85, "analysis": "总体分析内容", "feedbackItems": [{"criterion": "核心定义", "covered": true, "userContent": "用户在该标准下的回答内容"}, {"criterion": "关键特性", "covered": false, "userContent": ""}], "recommendedAnswer": "系统推荐的完整标准答案"}。feedbackItems数组中的每个元素对应一个评价标准，criterion是评价标准名称，covered表示用户是否覆盖了该标准（true/false），userContent是用户答案中涉及该标准的内容（如果covered为false则userContent为空字符串）。recommendedAnswer是系统推荐的完整标准答案。问题：{{vars}}', '答案评分的Prompt模板');

-- 2. 初始化知识点数据（树形结构）
-- MySQL 相关知识点
INSERT INTO lc_knowledge (parent_id, name, description, level) VALUES
(0, 'MySQL基础', 'MySQL数据库基础知识', 1),
(0, 'Java基础', 'Java编程语言基础知识', 1),
(0, 'Spring Boot', 'Spring Boot框架知识', 1),
(0, 'Redis', 'Redis缓存数据库知识', 1);

-- MySQL 子知识点
INSERT INTO lc_knowledge (parent_id, name, description, level) VALUES
((SELECT id FROM lc_knowledge WHERE name = 'MySQL基础' LIMIT 1), 'MySQL索引', 'MySQL索引原理和使用', 2),
((SELECT id FROM lc_knowledge WHERE name = 'MySQL基础' LIMIT 1), 'MySQL事务', 'MySQL事务ACID特性', 2),
((SELECT id FROM lc_knowledge WHERE name = 'MySQL基础' LIMIT 1), 'MySQL锁机制', 'MySQL锁的类型和使用场景', 2),
((SELECT id FROM lc_knowledge WHERE name = 'MySQL基础' LIMIT 1), 'MySQL优化', 'MySQL查询优化和性能调优', 2);

-- Java 子知识点
INSERT INTO lc_knowledge (parent_id, name, description, level) VALUES
((SELECT id FROM lc_knowledge WHERE name = 'Java基础' LIMIT 1), 'Java集合', 'Java集合框架', 2),
((SELECT id FROM lc_knowledge WHERE name = 'Java基础' LIMIT 1), 'Java多线程', 'Java并发编程', 2),
((SELECT id FROM lc_knowledge WHERE name = 'Java基础' LIMIT 1), 'JVM原理', 'Java虚拟机原理和调优', 2),
((SELECT id FROM lc_knowledge WHERE name = 'Java基础' LIMIT 1), '设计模式', 'Java设计模式', 2);

-- Spring Boot 子知识点
INSERT INTO lc_knowledge (parent_id, name, description, level) VALUES
((SELECT id FROM lc_knowledge WHERE name = 'Spring Boot' LIMIT 1), 'Spring IOC', 'Spring控制反转和依赖注入', 2),
((SELECT id FROM lc_knowledge WHERE name = 'Spring Boot' LIMIT 1), 'Spring AOP', 'Spring面向切面编程', 2),
((SELECT id FROM lc_knowledge WHERE name = 'Spring Boot' LIMIT 1), 'Spring MVC', 'Spring MVC框架原理', 2);

-- Redis 子知识点
INSERT INTO lc_knowledge (parent_id, name, description, level) VALUES
((SELECT id FROM lc_knowledge WHERE name = 'Redis' LIMIT 1), 'Redis数据结构', 'Redis五种基本数据结构', 2),
((SELECT id FROM lc_knowledge WHERE name = 'Redis' LIMIT 1), 'Redis持久化', 'RDB和AOF持久化机制', 2),
((SELECT id FROM lc_knowledge WHERE name = 'Redis' LIMIT 1), 'Redis集群', 'Redis主从复制和哨兵模式', 2);

-- 3. 初始化用户掌握度数据（可选，用于测试）
-- 假设用户ID为1，初始掌握度为50
INSERT INTO lc_user_mastery (user_id, knowledge_id, mastery_score, last_answer_time) 
SELECT 1, id, 50, NULL FROM lc_knowledge WHERE level = 1;
