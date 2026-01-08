# AI 面试模拟平台 - 项目设置指南

## 前置条件

1. **Java 17+** 已安装
2. **Maven** 已安装并配置
3. **MySQL** 服务已启动
4. **Redis** 服务已启动

## 数据库设置

### 1. 创建数据库
```sql
CREATE DATABASE IF NOT EXISTS ai_mock CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_mock;
```

### 2. 执行建表SQL
执行 `src/main/resources/schema.sql` 文件中的SQL语句

### 3. 初始化数据（可选）
执行 `src/main/resources/init-data.sql` 文件中的SQL语句，插入示例数据

## 配置文件

修改 `src/main/resources/application.yml` 中的配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_mock?useSSL=false&characterEncoding=utf8mb4&serverTimezone=UTC
    username: root  # 修改为你的MySQL用户名
    password: root  # 修改为你的MySQL密码
  redis:
    host: localhost
    port: 6379
    database: 0

langchain4j:
  openai:
    api-key: ${OPENAI_API_KEY:your-api-key-here}  # 设置你的OpenAI API Key
    base-url: https://api.openai.com/v1
    model-name: gpt-4o-mini
```

## Maven 依赖下载

### 在 IDE 中（推荐）
1. 右键点击 `pom.xml` 文件
2. 选择 "Maven" -> "Reload Project" 或 "Update Project"
3. 等待依赖下载完成

### 命令行方式
```bash
# Windows PowerShell
.\mvnw.cmd clean install

# 或者如果有全局Maven
mvn clean install
```

## 启动项目

### 方式1：IDE中运行
1. 打开 `QuizMeUpApplication.java`
2. 右键选择 "Run 'QuizMeUpApplication'"

### 方式2：命令行运行
```bash
.\mvnw.cmd spring-boot:run
```

## API 使用说明

所有接口需要在请求头中携带 Token：
```
X-Token: local-dev-token
```

### 1. 生成面试题
```http
POST http://localhost:8080/api/interview/questions
Content-Type: application/json
X-Token: local-dev-token

{
  "userId": 1,
  "topic": "MySQL"
}
```

### 2. 提交答案
```http
POST http://localhost:8080/api/interview/submit
Content-Type: application/json
X-Token: local-dev-token

{
  "userId": 1,
  "questionId": 1,
  "answer": "我的答案是..."
}
```

### 3. 获取知识点树
```http
GET http://localhost:8080/api/interview/knowledge-tree?userId=1
X-Token: local-dev-token
```

## 常见问题

### 1. LangChain4j 依赖无法解析
- 确保网络连接正常，Maven 可以访问中央仓库
- 在 IDE 中刷新 Maven 项目
- 检查 `pom.xml` 中的依赖版本是否正确

### 2. 数据库连接失败
- 检查 MySQL 服务是否启动
- 检查 `application.yml` 中的数据库配置是否正确
- 确保数据库 `ai_mock` 已创建

### 3. Redis 连接失败
- 检查 Redis 服务是否启动
- 检查 `application.yml` 中的 Redis 配置是否正确

### 4. OpenAI API 调用失败
- 检查 API Key 是否正确设置
- 检查网络是否可以访问 OpenAI API
- 如果使用代理，可能需要配置代理设置

## 项目结构

```
src/main/java/com/example/quizmeup/
├── ai/                    # AI能力层
│   ├── LlmClient.java     # LLM客户端封装
│   └── PromptService.java # Prompt管理
├── config/                # 配置类
│   ├── LlmConfig.java     # LangChain4j配置
│   ├── RedisConfig.java   # Redis配置
│   └── WebConfig.java     # Web配置（拦截器）
├── domain/                # 领域层
│   └── entity/            # 实体类
├── infra/                 # 基础设施层
│   └── mapper/            # MyBatis Mapper
├── interfaces/            # 接口层（API网关+Controller）
│   ├── InterviewController.java
│   └── TokenInterceptor.java
├── service/               # 业务服务层
│   ├── dto/               # 数据传输对象
│   └── InterviewService.java
└── QuizMeUpApplication.java  # 启动类
```
