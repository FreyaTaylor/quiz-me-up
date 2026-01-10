# AI面试模拟平台 - 技术设计文档

## 📋 项目概述

AI面试模拟平台是一个基于Spring Boot + LangChain4j的本地部署面试练习系统。系统通过LLM生成个性化面试题目，对用户答案进行智能评分和分析，并跟踪用户的知识点掌握度。

### 核心功能
1. **用户认证**：基于SHA256密码哈希的登录系统
2. **知识树管理**：支持多层级知识树结构，每个叶节点关联多道题目
3. **智能题目生成**：基于LLM动态生成面试题目和标准答案
4. **答案评分**：使用LLM对用户答案进行多维度评分和反馈
5. **学习进度跟踪**：按知识点聚合计算掌握度，支持加权进度计算
6. **学习趋势分析**：可视化展示各领域的学习进度趋势

---

## 🛠 技术栈

### 后端
- **框架**: Spring Boot 3.3.5
- **ORM**: MyBatis-Plus 3.5.7
- **AI集成**: LangChain4j (OpenAI适配器)
- **数据库**: MySQL 8.0+
- **缓存**: Redis
- **JSON处理**: FastJSON2
- **工具库**: Lombok

### 前端
- **技术**: 原生HTML + JavaScript
- **UI框架**: Tailwind CSS (CDN)
- **存储**: sessionStorage (用户状态)

---

## 🏗 系统架构

### 分层架构

```
┌─────────────────────────────────────┐
│   API Gateway Layer (Controller)     │  ← 接口层
├─────────────────────────────────────┤
│   Business Service Layer             │  ← 业务逻辑层
├─────────────────────────────────────┤
│   AI Capability Layer                │  ← AI能力层
├─────────────────────────────────────┤
│   Data Access Layer (Mapper)         │  ← 数据访问层
├─────────────────────────────────────┤
│   Database (MySQL + Redis)           │  ← 数据存储层
└─────────────────────────────────────┘
```

### 核心组件

1. **API Gateway Layer**
   - `AuthController`: 用户认证接口
   - `InterviewController`: 面试相关接口
   - `KnowledgeController`: 知识树管理接口
   - `TokenInterceptor`: Token认证拦截器

2. **Business Service Layer**
   - `AuthService`: 用户认证服务
   - `InterviewService`: 面试业务逻辑
   - `KnowledgeService`: 知识树管理服务

3. **AI Capability Layer**
   - `LlmClient`: LLM调用封装
   - `PromptService`: Prompt模板管理

4. **Data Access Layer**
   - `UserMapper`: 用户数据访问
   - `KnowledgeMapper`: 知识点数据访问
   - `QuestionMapper`: 题目数据访问
   - `QuestionRecordMapper`: 答题记录数据访问
   - `UserMasteryMapper`: 掌握度数据访问

---

## 🗄 数据库设计

### 表结构

#### 1. users - 用户表
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**字段说明**:
- `id`: 用户ID，主键自增
- `username`: 用户名，唯一
- `password_hash`: 密码哈希值（SHA256）
- `created_at`: 创建时间

#### 2. lc_knowledge - 知识点表（树形结构）
```sql
CREATE TABLE lc_knowledge (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**字段说明**:
- `id`: 知识点ID（VARCHAR，全局唯一，如"java_concurrent_threadpool"）
- `parent_id`: 父节点ID
- `name`: 知识点名称
- `description`: 描述
- `level`: 层级（1为根节点）
- `path`: 路径（如"java_concurrent/threadpool"）
- `importance`: 重要性（1-5）
- `is_leaf`: 是否为叶节点（只有叶节点关联题目）

#### 3. lc_questions - 题目表
```sql
CREATE TABLE lc_questions (
    id VARCHAR(128) PRIMARY KEY,
    knowledge_id VARCHAR(128) NOT NULL,
    question_text TEXT NOT NULL,
    model_answer TEXT NOT NULL,
    difficulty TINYINT DEFAULT 3 COMMENT '难度 1-5',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_knowledge_id (knowledge_id),
    FOREIGN KEY (knowledge_id) REFERENCES lc_knowledge(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**字段说明**:
- `id`: 题目ID（如"java_concurrent_threadpool_q1"）
- `knowledge_id`: 关联的知识点ID（必须是叶节点）
- `question_text`: 题目内容
- `model_answer`: 标准答案
- `difficulty`: 难度（1-5）

#### 4. lc_question_record - 答题记录表
```sql
CREATE TABLE lc_question_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    question_id VARCHAR(128) NOT NULL,
    score TINYINT DEFAULT 0 COMMENT '得分 0-100',
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_question_id (question_id),
    INDEX idx_submitted_at (submitted_at),
    FOREIGN KEY (question_id) REFERENCES lc_questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**字段说明**:
- `id`: 记录ID
- `user_id`: 用户ID
- `question_id`: 题目ID
- `score`: 得分（0-100）
- `submitted_at`: 提交时间

#### 5. lc_user_mastery - 用户掌握度表
```sql
CREATE TABLE lc_user_mastery (
    user_id BIGINT NOT NULL,
    knowledge_id VARCHAR(128) NOT NULL,
    proficiency TINYINT NOT NULL DEFAULT 0 COMMENT '该知识点下所有题的平均分 0-100',
    total_questions INT NOT NULL DEFAULT 0 COMMENT '该知识点总题数',
    practiced_count INT NOT NULL DEFAULT 0 COMMENT '已答题数',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, knowledge_id),
    FOREIGN KEY (knowledge_id) REFERENCES lc_knowledge(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**字段说明**:
- `user_id`: 用户ID（复合主键）
- `knowledge_id`: 知识点ID（复合主键）
- `proficiency`: 掌握度（该知识点下所有题的平均分）
- `total_questions`: 总题数
- `practiced_count`: 已答题数

#### 6. lc_prompt_template - Prompt模板表
```sql
CREATE TABLE lc_prompt_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL UNIQUE,
    content TEXT NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**字段说明**:
- `code`: 模板代码（如"KNOWLEDGE_TREE_GEN"）
- `content`: 模板内容（支持{{vars}}变量替换）
- `description`: 描述

### 数据关系图

```
users (1) ──┐
            │
            ├──> lc_user_mastery (N) ──> lc_knowledge (1)
            │
            └──> lc_question_record (N) ──> lc_questions (1)
                                                │
                                                └──> lc_knowledge (1)
```

---

## 🔌 API接口文档

### 认证接口

#### POST /api/login
用户登录

**请求体**:
```json
{
  "username": "test",
  "password": "123456"
}
```

**响应**:
```json
{
  "userId": 1,
  "username": "test"
}
```

**错误响应**: 401 Unauthorized

---

### 知识树接口

#### GET /api/knowledge/tree
获取用户的知识树（带掌握度）

**请求参数**: `userId` (Long)

**响应**:
```json
[
  {
    "id": "java_concurrent",
    "name": "Java并发",
    "description": "Java并发编程相关知识",
    "parentId": null,
    "level": 1,
    "importance": 5,
    "isLeaf": false,
    "proficiency": 0,
    "totalQuestions": 0,
    "practicedCount": 0,
    "weightedProgress": 0.0,
    "children": [
      {
        "id": "java_concurrent_threadpool",
        "name": "线程池",
        "description": "线程池原理和使用",
        "parentId": "java_concurrent",
        "level": 2,
        "importance": 4,
        "isLeaf": true,
        "proficiency": 85,
        "totalQuestions": 5,
        "practicedCount": 3,
        "weightedProgress": 0.85,
        "children": []
      }
    ]
  }
]
```

#### PUT /api/knowledge/mindmap/node
更新节点信息（编辑节点）

**请求体**:
```json
{
  "nodeId": "java_concurrent_threadpool",
  "name": "线程池",
  "description": "线程池原理和使用详解",
  "importance": 4
}
```

**响应**:
```json
{
  "message": "节点更新成功"
}
```

**说明**:
- 可更新节点名称、描述、重要程度
- 所有字段都是可选的，只更新提供的字段

#### POST /api/knowledge/mindmap/expand
AI扩充子树

**请求体**:
```json
{
  "userId": 1,
  "nodeId": "java_concurrent_threadpool"
}
```

**响应**:
```json
{
  "message": "子树扩充成功"
}
```

**说明**:
- 只能扩充第二层级以下（level >= 2）的节点
- 只能扩充没有子节点的节点
- 调用LLM生成3-5个子节点
- 自动更新父节点的isLeaf状态

---

### 面试接口

#### GET /api/interview/topics
获取用户的知识点列表（叶节点，作为topic选择）

**请求参数**: `userId` (Long)

**响应**:
```json
[
  {
    "id": "java_concurrent_threadpool",
    "name": "线程池",
    "practicedCount": 3,
    "totalQuestions": 5,
    "proficiency": 85
  }
]
```

#### GET /api/interview/questions
获取指定知识点的所有题目（首次调用时生成）

**请求参数**:
- `userId` (Long): 用户ID
- `knowledgeId` (String): 知识点ID

**响应**:
```json
[
  {
    "id": "java_concurrent_threadpool_q1",
    "knowledgeId": "java_concurrent_threadpool",
    "questionText": "请解释线程池的核心参数",
    "difficulty": 3
  }
]
```

**说明**:
- 如果该知识点没有题目，会调用LLM生成
- 生成后保存到lc_questions表
- 更新lc_user_mastery的total_questions

#### POST /api/interview/submit
提交答案

**请求体**:
```json
{
  "userId": 1,
  "questionId": "java_concurrent_threadpool_q1",
  "answer": "线程池的核心参数包括..."
}
```

**响应**:
```json
{
  "score": 85,
  "analysis": "正确说明定长，但未提存储差异和适用场景",
  "recommendedAnswer": "CHAR是定长字符串类型...",
  "feedbackItems": [
    {
      "criterion": "核心定义",
      "covered": true,
      "userContent": "CHAR定长"
    },
    {
      "criterion": "关键特性",
      "covered": false,
      "userContent": ""
    }
  ]
}
```

**说明**:
- 调用LLM对答案进行评分
- 保存答题记录到lc_question_record
- 更新lc_user_mastery的proficiency和practicedCount

---

## 🎨 前端页面

### 1. interview.html - 答题页面

**功能**:
- 用户登录（模态框）
- 知识点选择（下拉框）
- 题目展示和答案输入
- 答案提交和评分展示
- 反馈表格和推荐回答展示

**关键逻辑**:
- 使用sessionStorage存储userId和username
- 所有API调用携带userId

**主要函数**:
- `checkLogin()`: 检查登录状态
- `loadTopics()`: 加载知识点列表
- `generateQuestions()`: 加载题目
- `submitAnswer(questionId)`: 提交答案
- `displayResult(questionId, result)`: 显示评分结果

### 2. mindmap.html - 思维导图页面

**功能**:
- 用户登录（模态框）
- 思维导图可视化展示（Canvas渲染）
- 节点选择（点击节点）
- 节点编辑（编辑名称、描述、重要程度）
- AI扩充子树（第二层级以下节点）
- 显示节点掌握度

**关键逻辑**:
- 使用Canvas绘制思维导图
- 树形布局算法计算节点位置
- 节点显示：名称、描述、重要程度、掌握度
- 支持节点选择和编辑
- AI扩充功能限制：只能扩充第二层级以下且无子节点的节点

**主要函数**:
- `checkLogin()`: 检查登录状态
- `loadMindmap()`: 加载知识树数据
- `renderMindmap()`: 渲染思维导图
- `calculateNodePositions()`: 计算节点位置（树形布局）
- `drawNodes(ctx)`: 绘制节点
- `drawConnections(ctx)`: 绘制连接线
- `editSelectedNode()`: 编辑选中的节点
- `saveNodeEdit()`: 保存节点编辑
- `expandSubtree()`: AI扩充子树

---

## 🔐 认证机制

### Token认证
- 所有API请求需要在Header中携带: `X-Token: local-dev-token`
- Token验证通过`TokenInterceptor`实现
- 登录接口(`/api/login`)被排除在拦截器之外

### 密码加密
- 开发阶段使用SHA256加密
- 密码哈希值存储在`users.password_hash`字段

---

## 🤖 AI集成

### Prompt模板

#### 1. KNOWLEDGE_TREE_GEN
生成知识树和题目

**用途**: 根据学习目标生成多层知识树结构及题目

**输入变量**:
- `learningGoal`: 学习目标（如"准备 Java 后端面试"）

**输出格式**:
```json
{
  "knowledgeTree": [
    {
      "id": "java_concurrent",
      "parentId": null,
      "name": "Java并发",
      "description": "...",
      "level": 1,
      "path": "java_concurrent",
      "importance": 5,
      "isLeaf": false
    }
  ],
  "questions": [
    {
      "id": "java_concurrent_threadpool_q1",
      "knowledgeId": "java_concurrent_threadpool",
      "questionText": "...",
      "modelAnswer": "...",
      "difficulty": 3
    }
  ]
}
```

#### 2. QUESTION_GEN_BY_KNOWLEDGE
按知识点生成题目

**用途**: 为指定知识点生成题目

**输入变量**:
- `knowledgeName`: 知识点名称
- `knowledgeDescription`: 知识点描述

**输出格式**:
```json
[
  {
    "questionText": "...",
    "modelAnswer": "...",
    "difficulty": 3
  }
]
```

#### 3. ANSWER_REVIEW
答案评分

**用途**: 对用户答案进行多维度评分

**输入变量**:
- `question`: 问题内容
- `answer`: 用户答案
- `modelAnswer`: 标准答案

**输出格式**:
```json
{
  "score": 85,
  "analysis": "总体分析内容",
  "feedbackItems": [
    {
      "criterion": "核心定义",
      "covered": true,
      "userContent": "用户在该标准下的回答内容"
    }
  ],
  "recommendedAnswer": "系统推荐的完整标准答案"
}
```

#### 4. EXPAND_SUBTREE
AI扩充子树

**用途**: 为指定节点生成子节点

**输入变量**:
- `parentName`: 父节点名称
- `parentDescription`: 父节点描述
- `parentLevel`: 父节点层级

**输出格式**:
```json
[
  {
    "id": "sub_topic_1",
    "name": "子知识点名称",
    "description": "详细的中文描述",
    "importance": 3,
    "isLeaf": true
  }
]
```

**说明**:
- 生成3-5个子节点
- id使用英文下划线命名
- importance为1-5
- isLeaf表示是否为叶节点

---

## 📊 业务逻辑

### 掌握度计算规则

#### 单个知识点掌握度
```
proficiency = AVG(lc_question_record.score) 
WHERE question.knowledge_id = knowledge_id
```

#### 一级领域进度
```
领域进度 = Σ(knowledge.proficiency × knowledge.importance) / Σ(importance × 100)
```

#### 整体加权完成率
```
整体进度 = Σ(所有叶节点 proficiency × importance) / Σ(所有叶节点 importance × 100)
```

### 题目生成流程

1. 用户选择知识点（叶节点）
2. 检查`lc_questions`表是否有该知识点的题目
3. 如果没有，调用LLM生成题目
4. 保存题目到`lc_questions`表
5. 更新`lc_user_mastery.total_questions`

### 答案提交流程

1. 用户提交答案
2. 调用LLM进行评分（传入问题、用户答案、标准答案）
3. 保存答题记录到`lc_question_record`
4. 重新计算该知识点的`proficiency`（平均分）
5. 更新`lc_user_mastery`的`proficiency`和`practicedCount`

---

## ⚙️ 配置说明

### application.yml

```yaml
spring:
  application:
    name: quiz-me-up
  datasource:
    url: jdbc:mysql://localhost:3306/interview_ai?characterEncoding=UTF-8
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  redis:
    host: localhost
    port: 6379
    database: 0

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
  global-config:
    db-config:
      id-type: auto
  type-aliases-package: com.example.quizmeup.domain.entity

langchain4j:
  openai:
    base-url: https://api.openai.com/v1
    api-key: ${OPENAI_API_KEY:dummy}  # 可使用环境变量
    model-name: gpt-4o-mini

server:
  port: 8080

logging:
  level:
    root: info
    com.example.quizmeup: debug
```

### WebConfig
- 配置`TokenInterceptor`拦截`/api/**`路径
- 排除`/api/login`路径

---

## 🚀 部署步骤

### 1. 环境准备
- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 2. 数据库初始化
```bash
# 1. 创建数据库
mysql -u root -p
CREATE DATABASE interview_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 2. 执行schema.sql
mysql -u root -p interview_ai < src/main/resources/schema.sql

# 3. 执行init-data.sql（可选，初始化Prompt模板和测试用户）
mysql -u root -p interview_ai < src/main/resources/init-data.sql
```

### 3. 配置修改
- 修改`application.yml`中的数据库连接信息
- 修改`application.yml`中的Redis连接信息
- 修改`application.yml`中的OpenAI API Key

### 4. 编译运行
```bash
# Windows
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw clean install
./mvnw spring-boot:run
```

### 5. 访问应用
- 答题页面: http://localhost:8080/interview.html
- 思维导图: http://localhost:8080/mindmap.html

---

## 🔨 从零开始重建项目

### 步骤1: 创建Spring Boot项目
```bash
# 使用Spring Initializr或IDE创建项目
# 选择依赖: Spring Web, Spring Data Redis, MySQL Driver, Lombok
```

### 步骤2: 添加依赖到pom.xml
参考"技术栈"章节的依赖列表，添加：
- MyBatis-Plus 3.5.7
- LangChain4j 0.34.0
- FastJSON2 2.0.53
- Spring Boot Starter Validation

### 步骤3: 创建项目结构
```
src/main/java/com/example/quizmeup/
├── domain/entity/          # 实体类（6个）
├── infra/mapper/          # Mapper接口（6个）
├── service/               # Service类（3个）
│   └── dto/              # DTO类（8个）
├── ai/                    # AI层（2个类）
├── interfaces/            # Controller（3个）
└── config/                # 配置类（3个）
```

### 步骤4: 创建数据库表
按照"数据库设计"章节的SQL脚本创建所有表。

### 步骤5: 实现实体类
按照"数据库设计"章节创建6个实体类：
1. `User.java`
2. `Knowledge.java`
3. `Question.java`
4. `QuestionRecord.java`
5. `UserMastery.java`
6. `PromptTemplate.java`

### 步骤6: 实现Mapper接口
创建6个Mapper接口，继承`BaseMapper<T>`：
1. `UserMapper.java`
2. `KnowledgeMapper.java`
3. `QuestionMapper.java`
4. `QuestionRecordMapper.java`
5. `UserMasteryMapper.java`
6. `PromptTemplateMapper.java`

### 步骤7: 实现DTO类
创建9个DTO类：
1. `LoginRequest.java`
2. `LoginResponse.java`
3. `KnowledgeNodeDTO.java`
4. `UpdateNodeRequest.java`
5. `ExpandSubtreeRequest.java`
6. `QuestionDTO.java`
7. `AnswerRequest.java`
8. `AnswerResult.java`
9. `FeedbackItem.java`

### 步骤8: 实现AI层
1. `PromptService.java` - Prompt模板管理
2. `LlmClient.java` - LLM调用封装

### 步骤9: 实现Service层
1. `AuthService.java` - 用户认证
2. `InterviewService.java` - 面试业务逻辑
3. `KnowledgeService.java` - 知识树管理

### 步骤10: 实现Controller层
1. `AuthController.java` - 认证接口
2. `InterviewController.java` - 面试接口
3. `KnowledgeController.java` - 知识树接口

### 步骤11: 实现配置类
1. `WebConfig.java` - Web配置（拦截器）
2. `LlmConfig.java` - LLM配置
3. `RedisConfig.java` - Redis配置

### 步骤12: 创建前端页面
1. `interview.html` - 答题页面
2. `mindmap.html` - 思维导图页面

### 步骤13: 初始化数据
执行`init-data.sql`初始化Prompt模板和测试用户。

### 步骤14: 配置application.yml
按照"配置说明"章节配置所有参数。

### 步骤15: 测试
1. 启动应用
2. 访问 http://localhost:8080/interview.html 或 http://localhost:8080/mindmap.html
3. 使用测试账号登录
4. 开始答题测试或查看思维导图

---

## 📝 开发规范

### 代码结构
```
src/main/java/com/example/quizmeup/
├── domain/entity/          # 实体类
├── infra/mapper/          # MyBatis Mapper
├── service/               # 业务服务层
│   └── dto/              # 数据传输对象
├── ai/                    # AI能力层
├── interfaces/            # Controller层
└── config/                # 配置类
```

### 命名规范
- 实体类: 使用`@TableName`指定表名
- Mapper接口: 继承`BaseMapper<T>`
- Service类: 使用`@Service`注解
- Controller类: 使用`@RestController`注解

### 数据库操作
- 使用MyBatis-Plus的`BaseMapper`进行CRUD
- 复杂查询使用`Wrappers`构建条件
- 复合主键更新使用`update(Entity, Wrapper)`而非`updateById`

---

## 🔄 更新日志

详细的更新日志请查看 [CHANGELOG.md](./CHANGELOG.md)

---

## 📚 参考资料

### 依赖版本
- Spring Boot: 3.3.5
- MyBatis-Plus: 3.5.7
- LangChain4j: 0.34.0
- FastJSON2: 2.0.53

### 关键依赖
```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.7</version>
</dependency>
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j</artifactId>
    <version>0.34.0</version>
</dependency>
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai</artifactId>
    <version>0.34.0</version>
</dependency>
<dependency>
    <groupId>com.alibaba.fastjson2</groupId>
    <artifactId>fastjson2</artifactId>
    <version>2.0.53</version>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

## 🐛 常见问题

### 1. MyBatis-Plus兼容性问题
**问题**: `Invalid bean definition with name 'knowledgeMapper'`
**解决**: 使用Spring Boot 3.3.5 + MyBatis-Plus 3.5.7

### 2. 复合主键更新问题
**问题**: `UserMastery`使用复合主键，不能使用`updateById`
**解决**: 使用`update(entity, wrapper)`方法

### 3. LLM返回格式问题
**问题**: LLM返回的JSON格式不正确
**解决**: 在Prompt中明确要求JSON格式，并添加格式示例

---

## 📞 联系方式

如有问题，请查看代码注释或提交Issue。

---

## 📖 文档维护说明

### 更新原则
1. **每次功能更新**：在`CHANGELOG.md`中记录版本更新
2. **每次架构变更**：更新本技术设计文档的对应章节
3. **每次数据库变更**：更新数据库设计章节，并记录迁移脚本
4. **每次API变更**：更新API接口文档章节

### 文档结构
- `README.md` - 项目简介和快速开始
- `TECHNICAL_DESIGN.md` - 完整技术设计文档（本文档）
- `CHANGELOG.md` - 版本更新日志

### 更新检查清单
- [ ] 更新版本号
- [ ] 更新CHANGELOG.md
- [ ] 更新TECHNICAL_DESIGN.md相关章节
- [ ] 更新数据库设计（如有变更）
- [ ] 更新API文档（如有变更）
- [ ] 更新部署步骤（如有变更）

---

**文档版本**: 1.1.0  
**最后更新**: 2024-01-08  
**维护者**: Development Team
