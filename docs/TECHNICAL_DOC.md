# Quiz Me Up - AI 面试模拟平台技术文档

## 📋 项目概述

Quiz Me Up 是一个本地部署的 AI 面试模拟平台，帮助用户通过 AI 生成的面试题进行学习和练习。系统使用 Spring Boot + MyBatis-Plus + LLM（DeepSeek）构建，支持知识点树形管理、智能题目生成、AI 答案评分等功能。

## 🛠️ 技术栈

### 后端
- **框架**: Spring Boot 3.5.9
- **ORM**: MyBatis-Plus 3.5.7
- **数据库**: MySQL 8.0
- **JSON 处理**: Jackson (Spring Boot 内置)
- **LLM 框架**: LangChain4j 0.34.0
- **LLM API**: DeepSeek API（兼容 OpenAI 格式）
- **Java 版本**: 17
- **构建工具**: Maven

### 前端
- **技术**: 原生 HTML/JavaScript
- **UI 框架**: Tailwind CSS (CDN)
- **存储**: sessionStorage

## 📁 项目结构

```
quiz-me-up/
├── src/main/java/com/example/quizmeup/
│   ├── ai/                    # AI 相关接口和实现
│   │   ├── LlmClient.java           # 默认 LLM 客户端（用于出题、评分）
│   │   ├── ReasonerLlmClient.java   # 推理模型客户端（用于知识树生成）
│   │   └── PromptService.java        # Prompt 模板服务
│   ├── common/                # 通用类
│   │   ├── FeResponse.java    # 统一响应封装
│   │   └── AiResponse.java    # AI 响应封装
│   ├── config/                # 配置类
│   │   ├── WebMvcConfig.java  # Web MVC 配置（拦截器注册）
│   │   └── LlmConfig.java     # LLM 模型配置
│   ├── controller/            # 控制器层
│   │   ├── UserController.java
│   │   ├── LearningController.java
│   │   ├── ProgressController.java
│   │   └── KnowledgeController.java
│   ├── dto/                   # 数据传输对象
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   ├── StartLearningRequest.java
│   │   ├── StartLearningResponse.java
│   │   ├── SubmitAnswerRequest.java
│   │   ├── SubmitAnswerResponse.java
│   │   ├── ProgressTreeRequest.java
│   │   ├── KnowledgeProgressNode.java
│   │   └── AnswerEvaluation.java
│   ├── entity/                # 实体类
│   │   ├── User.java
│   │   ├── Knowledge.java
│   │   ├── Question.java
│   │   ├── QuestionRecord.java
│   │   ├── UserMastery.java
│   │   └── PromptTemplate.java
│   ├── interceptor/           # 拦截器
│   │   └── AuthInterceptor.java
│   ├── mapper/                # Mapper 接口（MyBatis-Plus）
│   │   ├── UserMapper.java
│   │   ├── KnowledgeMapper.java
│   │   ├── QuestionMapper.java
│   │   ├── QuestionRecordMapper.java
│   │   ├── UserMasteryMapper.java
│   │   └── PromptTemplateMapper.java
│   ├── repository/            # Repository 层（封装数据访问）
│   │   ├── UserRepository.java
│   │   ├── KnowledgeRepository.java
│   │   ├── QuestionRepository.java
│   │   ├── QuestionRecordRepository.java
│   │   ├── UserMasteryRepository.java
│   │   └── PromptTemplateRepository.java
│   └── service/               # 服务层
│       ├── UserService.java          # 用户服务
│       ├── LearningService.java      # 学习服务（出题、评分）
│       ├── ProgressService.java      # 进度服务（掌握度计算）
│       └── StudyService.java         # 学习管理服务（知识树管理）
├── src/main/resources/
│   ├── static/                # 静态资源
│   │   ├── login.html         # 登录页面
│   │   ├── learning.html      # 学习页面
│   │   ├── progress.html      # 进度页面
│   │   └── admin.html         # 管理页面（知识树初始化）
│   └── application.properties # 配置文件
└── docs/
    ├── schema.sql             # 数据库表结构
    └── init_data.sql          # 初始化数据
```

## 🗄️ 数据库设计

### 核心表结构

1. **users** - 用户表
   - `id`: 用户ID（主键，自增）
   - `username`: 用户名（唯一）
   - `password`: 密码（明文存储）
   - `role`: 用户角色（ADMIN 为管理员，NULL 为普通用户）
   - `created_at`: 创建时间

2. **lc_knowledge** - 知识点表
   - `id`: 知识点ID（主键，如 "java.concurrent.threadpool"）
   - `parent_id`: 父节点ID（根节点为 NULL）
   - `name`: 知识点名称
   - `description`: 知识点描述
   - `level`: 层级（从 1 开始）
   - `importance`: 重要性（1-5，默认 3）
   - `is_leaf`: 是否为叶节点（只有叶节点可以关联题目）
   - `created_at`: 创建时间
   - `updated_at`: 更新时间

3. **lc_questions** - 题目表
   - `id`: 题目ID（主键，格式：`{knowledgeId}_Q{index}`）
   - `knowledge_id`: 关联知识点ID（外键，必须是叶节点）
   - `question_text`: 题目内容
   - `model_answer`: 标准答案
   - `importance`: 重要性（1-5，默认 3）
   - `difficulty`: 难度（1-5，默认 3）
   - `created_at`: 创建时间
   - `updated_at`: 更新时间

4. **lc_question_record** - 答题记录表
   - `id`: 记录ID（主键，自增）
   - `user_id`: 用户ID（外键）
   - `question_id`: 题目ID（外键）
   - `score`: 得分（0.0-100.0，保留 1 位小数）
   - `submitted_at`: 提交时间

5. **lc_user_mastery** - 用户掌握度表
   - `user_id`: 用户ID（复合主键，外键）
   - `knowledge_id`: 知识点ID（复合主键，外键）
   - `proficiency`: 掌握度（0.00-100.00，保留 2 位小数）
   - `updated_at`: 更新时间

6. **lc_prompt_template** - Prompt 模板表
   - `id`: 模板ID（主键）
   - `code`: 模板代码（如 "INTERVIEW_Q_GEN"）
   - `content`: 模板内容
   - `description`: 描述

详细表结构请参考 `docs/schema.sql`

## 🔌 API 接口文档

### 1. 用户认证

#### 登录
- **URL**: `POST /api/auth/login`
- **请求体**:
```json
{
  "username": "alice",
  "password": "123456"
}
```
- **响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "username": "alice"
  }
}
```

### 2. 知识点管理

#### 获取所有根节点
- **URL**: `GET /api/v1/knowledge/root-nodes`
- **响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "java",
      "name": "Java",
      "parentId": null,
      "isLeaf": false
    }
  ]
}
```

#### 根据根节点获取知识树
- **URL**: `POST /api/v1/knowledge/leaf-nodes-by-root`
- **请求体**:
```json
{
  "rootId": "java",
  "userId": 1
}
```
- **响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": "java",
    "name": "Java",
    "isLeaf": false,
    "proficiency": null,
    "children": [
      {
        "id": "java.concurrent",
        "name": "Java 并发",
        "isLeaf": false,
        "proficiency": null,
        "children": [...]
      }
    ]
  }
}
```

#### 初始化知识树
- **URL**: `POST /api/v1/knowledge/init`
- **请求体**:
```json
{
  "knowledgeRoot": "Java",
  "count": 20
}
```
- **响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": "知识树初始化成功"
}
```

### 3. 学习相关

#### 开始学习
- **URL**: `POST /api/v1/learning/start`
- **请求体**:
```json
{
  "userId": 1,
  "knowledgeId": "java.concurrent.threadpool"
}
```
- **响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "questionId": "java.concurrent.threadpool_Q1",
      "questionText": "请解释 Java 中线程池的工作原理？",
      "lastScore": 85.5
    },
    {
      "questionId": "java.concurrent.threadpool_Q2",
      "questionText": "线程池有哪些核心参数？",
      "lastScore": 0.0
    }
  ]
}
```

#### 提交答案
- **URL**: `POST /api/v1/learning/submit`
- **请求体**:
```json
{
  "userId": 1,
  "questionId": "java.concurrent.threadpool_q_1234567890",
  "answerText": "线程池通过复用线程来提高性能..."
}
```
- **响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "score": 85.5,
    "conclusion": "基本正确",
    "analysis": "答案基本覆盖了核心要点...",
    "referenceAnswer": "建议补充更多关于线程池参数配置的说明..."
  }
}
```

### 4. 进度相关

#### 获取学习进度树
- **URL**: `POST /api/v1/progress/tree`
- **请求体**:
```json
{
  "userId": 1
}
```
- **响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "java",
      "name": "Java",
      "proficiency": 75.50,
      "isLeaf": false,
      "children": [
        {
          "id": "java.concurrent",
          "name": "Java 并发",
          "proficiency": 80.00,
          "isLeaf": false,
          "children": [...]
        }
      ]
    }
  ]
}
```

## 🌐 页面访问

### 启动应用

1. **配置数据库**
   - 修改 `src/main/resources/application.properties` 中的数据库连接信息
   - 执行 `docs/schema.sql` 创建表结构
   - 执行 `docs/init_data.sql` 初始化测试数据

2. **配置 LLM API**（可选）
   - 修改 `src/main/resources/application.properties` 中的 `llm.api.key`
   - 如果不配置，系统会使用模拟响应（用于开发测试）

3. **启动应用**
   ```bash
   mvn spring-boot:run
   ```
   或使用 IDE 运行 `QuizmeupApplication.java`

4. **访问页面**
   - 应用默认运行在 `http://localhost:8080`
   - **学习页面**: http://localhost:8080/learning.html
   - **进度页面**: http://localhost:8080/progress.html

> **提示**: Spring Boot 会自动将 `src/main/resources/static/` 目录下的文件作为静态资源提供，无需额外配置。

### 测试账号

根据 `docs/init_data.sql`，系统预置了以下测试账号：
- 用户名: `alice`，密码: `123456`
- 用户名: `bob`，密码: `password`

## 🔐 认证机制

- 所有 `/api/**` 接口（除 `/api/auth/**`）需要携带 `userId`
- 前端通过 `sessionStorage` 存储 `userId`
- 后端从请求参数 `userId` 或 Header `userId` 获取
- `AuthInterceptor` 会验证 `userId` 是否存在于 `users` 表

## 📊 核心业务逻辑

### 1. 知识树初始化流程
1. 管理员调用初始化接口，传入根节点名称和节点数量
2. 使用 `KNOWLEDGE_TREE_GEN` Prompt 模板生成请求
3. 调用 `ReasonerLlmClient`（推理模型）生成知识树结构
4. 解析 LLM 返回的 JSON，递归保存到数据库
5. 自动判断叶节点（无子节点的节点为叶节点）

### 2. 题目生成流程
1. 用户选择知识点（必须是叶节点）
2. 查询该知识点下是否已有题目
3. 如果没有，调用 LLM 生成新题目（使用 `INTERVIEW_Q_GEN` 模板）
4. 保存题目到数据库（题目ID格式：`{knowledgeId}_Q{index}`）
5. 查询用户在该知识点下的最近一次得分
6. 返回题目列表（包含最近得分）

### 3. 答案评分流程
1. 用户提交答案
2. 从数据库获取题目和标准答案
3. 使用 `ANSWER_EVALUATION` Prompt 模板构造评分请求
4. 调用 `LlmClient`（默认模型）进行评分
5. 解析 LLM 响应，提取评分结果（score, conclusion, analysis, referenceAnswer）
6. 保存答题记录到 `lc_question_record`
7. 更新用户掌握度（使用悲观锁防止并发问题）

### 4. 掌握度计算
- **叶节点**: 直接从 `lc_user_mastery.proficiency` 查询
  - 计算公式：该知识点下所有题目的平均分 = Σ(题目得分) / 题目数量
- **非叶节点**: 加权平均 = Σ(child.proficiency × child.importance) / Σ(child.importance)
  - 递归计算，从叶节点向上聚合

### 5. 并发控制
- 使用数据库悲观锁（`SELECT FOR UPDATE`）防止掌握度更新时的并发问题
- 在 `updateUserMastery` 方法中，先锁定记录，再重新查询所有答题记录，确保数据一致性

## ⚙️ 配置说明

### application.properties 关键配置

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/quizmeup
spring.datasource.username=root
spring.datasource.password=root

# LLM API 配置（DeepSeek）
llm.api.url=https://api.deepseek.com/v1
llm.api.key=your-deepseek-api-key
llm.api.model=deepseek-chat
llm.tree.api.reasonerModel=deepseek-reasoner
```

> **注意**: 如果不配置 `llm.api.key`，系统会使用模拟响应，适合开发测试。

## 🚀 部署说明

1. **打包应用**
   ```bash
   mvn clean package
   ```

2. **运行 JAR**
   ```bash
   java -jar target/quizmeup-0.0.1-SNAPSHOT.jar
   ```

3. **生产环境建议**
   - 配置 HTTPS
   - 使用环境变量管理敏感配置（如数据库密码、API Key）
   - 配置日志级别
   - 设置 JVM 参数

## 📝 开发规范

1. **分层架构**: Controller → Service → Repository → Mapper
   - Controller 层：只负责参数校验、调用 Service、封装响应，不做业务逻辑
   - Service 层：业务逻辑处理，事务控制
   - Repository 层：封装所有数据访问逻辑，使用 LambdaQueryWrapper
   - Mapper 层：MyBatis-Plus 接口，不直接暴露给 Service

2. **依赖注入**: 统一使用 `@RequiredArgsConstructor` + `final` 字段，禁止使用 `@Autowired`

3. **禁止硬编码 Prompt**: 必须通过 `PromptService.render()` 获取模板，禁止在代码中硬编码 Prompt 字符串

4. **统一响应格式**: 使用 `FeResponse<T>` 封装响应，所有接口返回统一格式

5. **事务控制**: 涉及数据库写操作使用 `@Transactional`，确保数据一致性

6. **异常处理**: 
   - 业务异常使用 `IllegalArgumentException`
   - 系统异常统一处理，Controller 层捕获并返回错误响应
   - 禁止在 Controller 层抛出未处理的异常

7. **数据访问规范**:
   - 禁止在 Service 层直接使用 Mapper
   - 必须通过 Repository 层访问数据
   - 使用 LambdaQueryWrapper 构建查询条件，禁止使用字符串字段名

8. **代码风格**:
   - 使用 Lombok 简化代码（@Data, @RequiredArgsConstructor）
   - 方法必须有 JavaDoc 注释
   - 魔法数字提取为常量
   - 统一使用 Java 17 语法特性

## 🔧 常见问题

### Q: LLM API 调用失败？
A: 检查 `llm.api.key` 是否正确配置，或使用模拟模式（不配置 key）

### Q: 数据库连接失败？
A: 检查数据库服务是否启动，连接信息是否正确

### Q: 静态页面无法访问？
A: 确保 HTML 文件在 `src/main/resources/static/` 目录下

### Q: 认证失败？
A: 检查请求是否携带 `userId` 参数或 Header

## 📚 相关文档

- [数据库表结构](./schema.sql)
- [初始化数据脚本](./init_data.sql)
- [项目规范](../.cursor/rules/java-backend.mdc)

## 📞 技术支持

如有问题，请查看项目代码注释或联系开发团队。

---

**最后更新**: 2024年12月
