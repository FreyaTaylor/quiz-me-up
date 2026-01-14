# Quiz Me Up - AI 面试模拟平台技术文档

## 📋 项目概述

Quiz Me Up 是一个本地部署的 AI 面试模拟平台，帮助用户通过 AI 生成的面试题进行学习和练习。系统使用 Spring Boot + MyBatis-Plus + LLM（DeepSeek）构建，支持知识点树形管理、智能题目生成、AI 答案评分等功能。

## 🛠️ 技术栈

### 后端
- **框架**: Spring Boot 3.5.9
- **ORM**: MyBatis-Plus 3.5.7
- **数据库**: MySQL 8.0
- **JSON 处理**: FastJSON2 2.0.43
- **LLM**: DeepSeek API（兼容 OpenAI 格式）
- **Java 版本**: 17

### 前端
- **技术**: 原生 HTML/JavaScript
- **UI 框架**: Tailwind CSS (CDN)
- **存储**: sessionStorage

## 📁 项目结构

```
quiz-me-up/
├── src/main/java/com/example/quizmeup/
│   ├── ai/                    # AI 相关接口和实现
│   │   ├── LlmClient.java
│   │   ├── PromptService.java
│   │   └── impl/
│   │       ├── LlmClientImpl.java
│   │       └── PromptServiceImpl.java
│   ├── common/                # 通用类
│   │   └── Result.java        # 统一响应封装
│   ├── config/                # 配置类
│   │   └── WebMvcConfig.java  # Web MVC 配置
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
│   ├── mapper/                # Mapper 接口
│   │   ├── UserMapper.java
│   │   ├── KnowledgeMapper.java
│   │   ├── QuestionMapper.java
│   │   ├── QuestionRecordMapper.java
│   │   ├── UserMasteryMapper.java
│   │   └── PromptTemplateMapper.java
│   └── service/               # 服务层
│       ├── UserService.java
│       ├── LearningService.java
│       ├── ProgressService.java
│       └── StudyService.java
├── src/main/resources/
│   ├── static/                # 静态资源
│   │   ├── learning.html      # 学习页面
│   │   └── progress.html      # 进度页面
│   ├── mapper/                # MyBatis XML 映射文件
│   └── application.properties # 配置文件
└── docs/
    ├── schema.sql             # 数据库表结构
    └── init_data.sql          # 初始化数据
```

## 🗄️ 数据库设计

### 核心表结构

1. **users** - 用户表
   - `id`: 用户ID（主键）
   - `username`: 用户名（唯一）
   - `password`: 密码（明文）

2. **lc_knowledge** - 知识点表
   - `id`: 知识点ID（主键，如 "java.concurrent.threadpool"）
   - `parent_id`: 父节点ID
   - `name`: 知识点名称
   - `is_leaf`: 是否为叶节点
   - `importance`: 重要性（1-5）

3. **lc_questions** - 题目表
   - `id`: 题目ID（主键）
   - `knowledge_id`: 关联知识点ID
   - `question_text`: 题目内容
   - `model_answer`: 标准答案

4. **lc_question_record** - 答题记录表
   - `id`: 记录ID（主键）
   - `user_id`: 用户ID
   - `question_id`: 题目ID
   - `score`: 得分（0.0-100.0）

5. **lc_user_mastery** - 用户掌握度表
   - `user_id`: 用户ID（复合主键）
   - `knowledge_id`: 知识点ID（复合主键）
   - `proficiency`: 掌握度（0.00-100.00）

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

### 2. 学习相关

#### 获取叶节点知识点
- **URL**: `GET /api/v1/knowledge/leaf-nodes`
- **响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "java.concurrent.threadpool",
      "name": "线程池",
      "isLeaf": true
    }
  ]
}
```

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
  "data": {
    "questionId": "java.concurrent.threadpool_q_1234567890",
    "questionText": "请解释 Java 中线程池的工作原理？"
  }
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

### 3. 进度相关

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

### 1. 题目生成流程
1. 用户选择知识点（必须是叶节点）
2. 查询该知识点下是否已有题目
3. 如果没有，调用 LLM 生成新题目
4. 保存题目到数据库
5. 返回题目给用户

### 2. 答案评分流程
1. 用户提交答案
2. 从数据库获取题目和标准答案
3. 使用 Prompt 模板构造评分请求
4. 调用 LLM 进行评分
5. 解析 LLM 响应，提取评分结果
6. 保存答题记录
7. 更新用户掌握度

### 3. 掌握度计算
- **叶节点**: 直接从 `lc_user_mastery.proficiency` 查询
- **非叶节点**: 加权平均 = Σ(child.proficiency × child.importance) / Σ(child.importance)

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
llm.api.timeout=30
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

1. **分层架构**: Controller → Service → Mapper
2. **禁止硬编码 Prompt**: 必须通过 `PromptService.render()` 获取模板
3. **统一响应格式**: 使用 `Result<T>` 封装响应
4. **事务控制**: 涉及数据库写操作使用 `@Transactional`
5. **异常处理**: 业务异常使用 `IllegalArgumentException`，系统异常统一处理

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

**最后更新**: 2024年
