# 快速开始指南

## 🚀 快速启动

### 1. 环境准备

- **Java**: JDK 17+
- **数据库**: MySQL 8.0+
- **Maven**: 3.6+

### 2. 数据库初始化

```bash
# 1. 创建数据库
mysql -u root -p
CREATE DATABASE quizmeup CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 2. 执行建表脚本
mysql -u root -p quizmeup < docs/schema.sql

# 3. 初始化测试数据
mysql -u root -p quizmeup < docs/init_data.sql
```

### 3. 配置应用

编辑 `src/main/resources/application.properties`:

```properties
# 数据库配置（根据实际情况修改）
spring.datasource.url=jdbc:mysql://localhost:3306/quizmeup
spring.datasource.username=root
spring.datasource.password=your_password

# LLM API 配置（DeepSeek）
llm.api.url=https://api.deepseek.com/v1
llm.api.key=your-deepseek-api-key
llm.api.model=deepseek-chat
```

> **注意**: 如果不配置 `llm.api.key`，系统会使用模拟响应，适合开发测试。

### 4. 启动应用

```bash
# 方式1: Maven
mvn spring-boot:run

# 方式2: IDE
运行 QuizmeupApplication.java

# 方式3: JAR 包
mvn clean package
java -jar target/quizmeup-0.0.1-SNAPSHOT.jar
```

### 5. 访问页面

应用启动后，默认运行在 **http://localhost:8080**

#### 🔐 登录页面
**访问地址**: http://localhost:8080/login.html

**功能**:
- 用户登录（测试账号：alice/123456 或 bob/password）
- 登录成功后跳转到学习页面

#### 📚 学习页面
**访问地址**: http://localhost:8080/learning.html

**功能**:
- 选择知识点（必须是叶节点）
- 获取题目列表（包含最近一次得分）
- 提交答案
- 查看 AI 评分结果（包含得分、评价、分析和参考答案）

#### 📊 进度页面
**访问地址**: http://localhost:8080/progress.html

**功能**:
- 输入用户ID
- 查看完整知识树结构
- 查看每个知识点的掌握度（叶节点显示实际掌握度，非叶节点显示加权平均）
- 展开/折叠节点

#### ⚙️ 管理页面
**访问地址**: http://localhost:8080/admin.html

**功能**:
- 初始化知识树（调用 LLM 生成知识树结构）
- 输入根节点名称（如 "Java", "Python"）
- 设置建议的节点数量
- 系统会自动生成并保存知识树到数据库

## 🧪 测试流程

### 方式一：完整流程测试

1. **初始化知识树（可选）**
   ```
   http://localhost:8080/admin.html
   ```
   - 输入根节点名称：`Java`
   - 输入节点数量：`20`
   - 点击"初始化知识树"
   - 等待 LLM 生成知识树结构

2. **登录**
   ```
   http://localhost:8080/login.html
   ```
   - 用户名: `alice`
   - 密码: `123456`
   - 登录成功后自动跳转到学习页面

3. **开始学习**
   - 在学习页面选择知识点（必须是叶节点，如：线程池）
   - 点击"开始学习"
   - 系统会生成或获取该知识点下的所有题目
   - 显示每个题目的最近一次得分

4. **提交答案**
   - 选择一个题目
   - 在文本框中输入答案
   - 点击"提交答案"
   - 查看 AI 评分结果（包含得分、评价、分析和参考答案）

5. **查看进度**
   ```
   http://localhost:8080/progress.html
   ```
   - 输入用户ID: `1`
   - 点击"加载进度"
   - 查看完整知识树和每个节点的掌握度
   - 叶节点显示实际掌握度，非叶节点显示加权平均掌握度

### 方式二：API 测试

使用 curl 或 Postman 测试 API 接口：

```bash
# 1. 登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"123456"}'

# 2. 获取根节点
curl -X GET http://localhost:8080/api/v1/knowledge/root-nodes

# 3. 根据根节点获取知识树
curl -X POST http://localhost:8080/api/v1/knowledge/leaf-nodes-by-root \
  -H "Content-Type: application/json" \
  -d '{"rootId":"java","userId":1}'

# 4. 开始学习（需要 userId 参数）
curl -X POST "http://localhost:8080/api/v1/learning/start?userId=1" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"knowledgeId":"java.concurrent.threadpool"}'

# 5. 提交答案
curl -X POST "http://localhost:8080/api/v1/learning/submit?userId=1" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"questionId":"java.concurrent.threadpool_Q1","answerText":"线程池是..."}'

# 6. 获取学习进度
curl -X POST http://localhost:8080/api/v1/progress/tree \
  -H "Content-Type: application/json" \
  -d '{"userId":1}'
```

## 📋 测试数据

系统预置了以下测试数据：

### 用户
- `alice` / `123456`
- `bob` / `password`

### 知识点树
```
Java
└── Java 并发
    ├── 线程池 (叶节点)
    └── 锁机制 (叶节点)
```

### Prompt 模板
- `INTERVIEW_Q_GEN` - 生成面试题
- `ANSWER_EVALUATION` - 答案评分

## 🔍 验证安装

### 检查数据库连接
```bash
mysql -u root -p quizmeup -e "SELECT COUNT(*) FROM users;"
# 应该返回 2（两个测试用户）
```

### 检查 API 接口
```bash
# 测试登录接口
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"123456"}'

# 应该返回包含 userId 的 JSON 响应
```

## ❗ 常见问题

### 端口被占用
修改 `application.properties` 添加：
```properties
server.port=8081
```

### 数据库连接失败
- 检查 MySQL 服务是否启动
- 检查用户名密码是否正确
- 检查数据库是否存在

### 静态页面 404
- 确保 HTML 文件在 `src/main/resources/static/` 目录
- 重启应用

### LLM API 调用失败
- 检查 API Key 是否正确
- 检查网络连接
- 查看应用日志

## 📞 获取帮助

- 查看完整技术文档: [TECHNICAL_DOC.md](./TECHNICAL_DOC.md)
- 查看数据库结构: [schema.sql](./schema.sql)
