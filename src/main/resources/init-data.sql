-- ============================================
-- 初始化数据 SQL 脚本
-- ============================================

-- 1. 初始化 Prompt 模板数据
INSERT INTO lc_prompt_template (code, content, description) VALUES
('KNOWLEDGE_TREE_GEN', '你是技术教育专家，请根据学习目标生成知识树结构（3-5层级）和题目。返回JSON格式：{"knowledgeTree": [{"id": "java_concurrent", "parentId": null, "name": "Java并发", "description": "...", "level": 1, "path": "java_concurrent", "importance": 5, "isLeaf": false}, {"id": "java_concurrent_threadpool", "parentId": "java_concurrent", "name": "线程池", "description": "...", "level": 2, "path": "java_concurrent/threadpool", "importance": 4, "isLeaf": true}], "questions": [{"id": "java_concurrent_threadpool_q1", "knowledgeId": "java_concurrent_threadpool", "questionText": "请解释线程池的核心参数", "modelAnswer": "核心参数包括corePoolSize、maximumPoolSize、keepAliveTime等", "difficulty": 3}]}。id使用英文下划线命名，全局唯一。每个叶节点生成3-5道题目。', '生成知识树和题目的Prompt模板'),
('QUESTION_GEN_BY_KNOWLEDGE', '你是技术面试官，请为知识点"{{knowledgeName}}"生成5-8道面试题，返回JSON数组格式：[{"questionText": "问题内容", "modelAnswer": "标准答案", "difficulty": 3}]。确保问题有针对性且实用，难度1-5。', '按知识点生成题目的Prompt模板'),
('ANSWER_REVIEW', '你是技术面试官，请对以下答案进行详细评估。首先分析问题的关键评价标准（通常4-6个，如：核心定义、关键特性、使用场景/注意事项、常见误区等），然后对用户答案进行评分和分析。返回JSON格式：{"score": 85, "analysis": "总体分析内容", "feedbackItems": [{"criterion": "核心定义", "covered": true, "userContent": "用户在该标准下的回答内容"}, {"criterion": "关键特性", "covered": false, "userContent": ""}], "recommendedAnswer": "系统推荐的完整标准答案"}。feedbackItems数组中的每个元素对应一个评价标准，criterion是评价标准名称，covered表示用户是否覆盖了该标准（true/false），userContent是用户答案中涉及该标准的内容（如果covered为false则userContent为空字符串）。recommendedAnswer是系统推荐的完整标准答案。问题：{{vars}}', '答案评分的Prompt模板'),
('EXPAND_SUBTREE', '你是技术教育专家，请为知识点"{{parentName}}"（描述：{{parentDescription}}，当前层级：{{parentLevel}}）生成3-5个子知识点。返回JSON数组格式：[{"id": "sub_topic_1", "name": "子知识点名称", "description": "详细的中文描述", "importance": 3, "isLeaf": true}]。id使用英文下划线命名，importance为1-5，isLeaf表示是否为叶节点。确保子知识点与父知识点相关且有意义。', 'AI扩充子树的Prompt模板');

-- 2. 初始化知识点数据（树形结构）
-- MySQL 相关知识点
INSERT INTO lc_knowledge (id, parent_id, name, description, level, path, importance, is_leaf) VALUES
('mysql_basic', NULL, 'MySQL基础', 'MySQL数据库基础知识', 1, 'mysql_basic', 3, FALSE),
('java_basic', NULL, 'Java基础', 'Java编程语言基础知识', 1, 'java_basic', 3, FALSE),
('spring_boot', NULL, 'Spring Boot', 'Spring Boot框架知识', 1, 'spring_boot', 3, FALSE),
('redis', NULL, 'Redis', 'Redis缓存数据库知识', 1, 'redis', 3, FALSE);

-- MySQL 子知识点
INSERT INTO lc_knowledge (id, parent_id, name, description, level, path, importance, is_leaf) VALUES
('mysql_basic_index', 'mysql_basic', 'MySQL索引', 'MySQL索引原理和使用', 2, 'mysql_basic/index', 3, TRUE),
('mysql_basic_transaction', 'mysql_basic', 'MySQL事务', 'MySQL事务ACID特性', 2, 'mysql_basic/transaction', 3, TRUE),
('mysql_basic_lock', 'mysql_basic', 'MySQL锁机制', 'MySQL锁的类型和使用场景', 2, 'mysql_basic/lock', 3, TRUE),
('mysql_basic_optimize', 'mysql_basic', 'MySQL优化', 'MySQL查询优化和性能调优', 2, 'mysql_basic/optimize', 3, TRUE);

-- Java 子知识点
INSERT INTO lc_knowledge (id, parent_id, name, description, level, path, importance, is_leaf) VALUES
('java_basic_collection', 'java_basic', 'Java集合', 'Java集合框架', 2, 'java_basic/collection', 3, TRUE),
('java_basic_multithread', 'java_basic', 'Java多线程', 'Java并发编程', 2, 'java_basic/multithread', 3, TRUE),
('java_basic_jvm', 'java_basic', 'JVM原理', 'Java虚拟机原理和调优', 2, 'java_basic/jvm', 3, TRUE),
('java_basic_design_pattern', 'java_basic', '设计模式', 'Java设计模式', 2, 'java_basic/design_pattern', 3, TRUE);

-- Spring Boot 子知识点
INSERT INTO lc_knowledge (id, parent_id, name, description, level, path, importance, is_leaf) VALUES
('spring_boot_ioc', 'spring_boot', 'Spring IOC', 'Spring控制反转和依赖注入', 2, 'spring_boot/ioc', 3, TRUE),
('spring_boot_aop', 'spring_boot', 'Spring AOP', 'Spring面向切面编程', 2, 'spring_boot/aop', 3, TRUE),
('spring_boot_mvc', 'spring_boot', 'Spring MVC', 'Spring MVC框架原理', 2, 'spring_boot/mvc', 3, TRUE);

-- Redis 子知识点
INSERT INTO lc_knowledge (id, parent_id, name, description, level, path, importance, is_leaf) VALUES
('redis_data_structure', 'redis', 'Redis数据结构', 'Redis五种基本数据结构', 2, 'redis/data_structure', 3, TRUE),
('redis_persistence', 'redis', 'Redis持久化', 'RDB和AOF持久化机制', 2, 'redis/persistence', 3, TRUE),
('redis_cluster', 'redis', 'Redis集群', 'Redis主从复制和哨兵模式', 2, 'redis/cluster', 3, TRUE);

-- 3. 初始化用户掌握度数据（可选，用于测试）
-- 假设用户ID为1，初始掌握度为0
-- 注意：lc_user_mastery表结构已变更，不再使用mastery_score和last_answer_time字段

-- 4. 初始化测试用户（密码为 "123456"，SHA256后为：8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92）
-- 注意：实际使用时请修改密码
INSERT INTO users (username, password_hash) VALUES
('1', '1'),
('2', '2');
