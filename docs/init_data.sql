-- ============================================
-- 初始化测试数据脚本
-- 用于 AI 面试平台预置数据
-- MySQL 8.0 兼容
-- ============================================

-- 1. 插入用户数据（明文密码）
INSERT INTO users (username, password, role) VALUES
('1', '123456', NULL),
('bob', 'password', NULL),
('admin', 'admin123', 'ADMIN');

-- 3. 插入 Prompt 模板
-- 生成面试题模板
INSERT INTO quizmeup.lc_prompt_template (id, code, content, description, created_at, updated_at) VALUES
(1, 'INTERVIEW_Q_GEN', '你是一位资深后端技术面试官，请根据我提供的知识点名称，生成 3 到 10 道高质量的面试题。

# 要求
- 知识点：{topic}
- 生成数量：3-10 道
- 每道题必须针对该主题的核心知识点，具有实用性和区分度

# 禁止
- 不要任何解释、序号、Markdown 代码块（如 ```json）
- 不要额外字段、注释或换行
- 不要输出除 JSON 外的任何字符

# 输出示例（仅作格式参考，不要输出此行）
{
"data":
  [
    {
      "questionText": "Java 中的 ThreadPoolExecutor 构造函数有哪些核心参数？分别代表什么含义？",
      "modelAnswer": "1. corePoolSize：线程池中保持存活的最小线程数...\\n2. maximumPoolSize：线程池允许创建的最大线程数...\\n3. keepAliveTime：当线程数超过 corePoolSize 时...",
      "importance": 5,
      "difficulty": 4
    },
    {
      "questionText": "线程池的拒绝策略有哪些？各自适用场景是什么？",
      "modelAnswer": "1. AbortPolicy（默认）：直接抛出异常...\\n2. CallerRunsPolicy：由调用线程执行任务...\\n3. DiscardPolicy：静默丢弃任务...",
      "importance": 4,
      "difficulty": 3
    }
  ]
}

现在开始输出：', '生成面试题', '2026-01-14 09:43:13', '2026-01-15 13:12:25');
INSERT INTO quizmeup.lc_prompt_template (id, code, content, description, created_at, updated_at) VALUES (2, 'ANSWER_EVALUATION', '你是一位资深的 Java 技术面试官，请对以下面试答案进行评分。

题目：{{question}}
考生答案：{{answer}}


返回内容：
score：总体评分（0–100 整数），基于完整性、技术准确性、深度、表达清晰度
analysis：对答案的简要分析（100–200 字）
feedbackItems：数组，列出答案应覆盖的关键评分点，每项含：
  criterion：关键词或关键要求（如“悲观锁核心思想”）
  covered：布尔值，表示考生是否提及该点
  userContent：考生回答中对应原文（若未覆盖则为空字符串）
  recommendedAnswer：一份准确、完整、包含典型示例的标准参考答案


# 禁止
- 不要任何解释、序号、Markdown 代码块（如 ```json）
- 不要额外字段、注释或换行
- 不要输出除 JSON 外的任何字符

# 输出示例（仅作格式参考，不要输出此行）
{
"data":
  {
  "score": 45,
  "analysis": "用户对乐观锁和悲观锁的核心思想有基本理解，但表述不够准确且缺乏关键细节。未提及版本号/时间戳机制（乐观锁核心），也未给出任何数据库层面的具体实现例子（如 SELECT FOR UPDATE 或 CAS 更新）。回答过于简略，无法体现对概念的深入掌握。",
  "feedbackItems": [
    {
      "criterion": "悲观锁核心思想",
      "covered": true,
      "userContent": " 悲观锁 是先加锁再操作"
    },
    {
      "criterion": "乐观锁的核心思想（不加锁，通过版本校验冲突）",
      "covered": false,
      "userContent": ""
    }
  ],
  "recommendedAnswer": "乐观锁假设并发冲突概率低，因此不加锁，而是在更新时检查数据是否被修改（通常通过 version 或 timestamp 字段）。例如：UPDATE user SET name=\'new\', version=version+1 WHERE id=1 AND version=10; 若 affected rows=0，则说明并发冲突。\\n\\n悲观锁假设冲突频繁，因此在读取数据时就加锁。例如在 MySQL 中使用：SELECT * FROM user WHERE id=1 FOR UPDATE; 此后其他事务无法修改该行，直到当前事务提交。\\n\\n乐观锁适合读多写少、冲突少的场景；悲观锁适合写多、强一致要求高的场景。"
}
}

现在开始输出：


', '答案评分', '2026-01-14 09:43:13', '2026-01-14 14:02:39');
INSERT INTO quizmeup.lc_prompt_template (id, code, content, description, created_at, updated_at) VALUES (3, 'KNOWLEDGE_TREE_GEN', '我正在准备中高级后端开发岗位的面试，希望系统性地梳理相关的知识体系。
请你以树形JSON结构返回一份完整的面试知识大纲，要求如下：
1.根节点表示整个{knowledgeRoot}知识体系；
2.每个节点包含以下字段：
id：一个简洁、唯一、英文单词或驼峰命名的标识符（如 threadCreation）；
name：该知识点的中文名称（如 “线程的创建方式”）；
importance：该知识点在中高级后端面试中的重要程度，取值为 1–5（5 为最高）；
children：子知识点列表（数组），若为叶子节点则为空数组 []；
3.所有叶子节点必须是具体、可考察的技术点（例如 “ArrayList”），不能是模糊或宽泛的概念（如 “集合”，为非叶子 节点），不能是具体问题（如“ArrayList的扩容机制”）；
4.知识体系应全部覆盖中高级后端面试的{knowledgeRoot}知识体系，知识点数量不小于{count}个

# 禁止
- 不要任何解释、序号、Markdown 代码块（如 ```json）
- 不要额外字段、注释或换行
- 不要输出除 JSON 外的任何字符

# 输出示例（仅作格式参考，不要输出此行）
{
"data": {
"id": "java",
"name": "Java",
"importance": "5",
"children": [
{
"id": "concurrent",
"name": "Java 并发",
"importance": "5",
"children": [
{
"id": "threadpool",
"name": "线程池",
"importance": "5",
"children": [
{
"id": "parameters",
"name": "线程池参数配置",
"importance": "5",
"children": []
}
]
}
]
}
]
}
}
现在开始输出：', '生成知识树', '2026-01-15 12:05:30', '2026-01-16 12:18:15');
