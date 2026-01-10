# 项目文件结构清单

本文档列出项目的所有文件及其作用，便于快速了解项目结构。

## 📁 项目根目录

```
quiz-me-up/
├── README.md                    # 项目简介和快速开始
├── TECHNICAL_DESIGN.md          # 完整技术设计文档
├── CHANGELOG.md                 # 版本更新日志
├── PROJECT_STRUCTURE.md         # 本文件：项目结构清单
├── pom.xml                      # Maven依赖配置
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/example/quizmeup/
    │   │       ├── domain/      # 领域层
    │   │       ├── infra/       # 基础设施层
    │   │       ├── service/     # 业务服务层
    │   │       ├── ai/          # AI能力层
    │   │       ├── interfaces/ # 接口层
    │   │       ├── config/      # 配置层
    │   │       └── QuizMeUpApplication.java  # 主启动类
    │   └── resources/
    │       ├── application.yml  # 应用配置
    │       ├── schema.sql       # 数据库表结构
    │       ├── init-data.sql    # 初始化数据
    │       └── static/          # 静态资源（前端页面）
    └── test/                     # 测试代码（可选）
```

---

## 📂 详细文件清单

### 1. 根目录文件

| 文件 | 说明 |
|------|------|
| `README.md` | 项目简介、快速开始指南 |
| `TECHNICAL_DESIGN.md` | 完整技术设计文档（核心文档） |
| `CHANGELOG.md` | 版本更新日志 |
| `PROJECT_STRUCTURE.md` | 项目结构清单（本文件） |
| `pom.xml` | Maven项目配置和依赖管理 |

---

### 2. 主启动类

| 文件 | 说明 |
|------|------|
| `QuizMeUpApplication.java` | Spring Boot主启动类，包含`@MapperScan`注解 |

---

### 3. 领域层 (domain)

#### 3.1 实体类 (entity)

| 文件 | 对应表 | 说明 |
|------|--------|------|
| `User.java` | `users` | 用户实体 |
| `Knowledge.java` | `lc_knowledge` | 知识点实体（树形结构） |
| `Question.java` | `lc_questions` | 题目实体 |
| `QuestionRecord.java` | `lc_question_record` | 答题记录实体 |
| `UserMastery.java` | `lc_user_mastery` | 用户掌握度实体（复合主键） |
| `PromptTemplate.java` | `lc_prompt_template` | Prompt模板实体 |

---

### 4. 基础设施层 (infra)

#### 4.1 Mapper接口 (mapper)

| 文件 | 对应实体 | 说明 |
|------|----------|------|
| `UserMapper.java` | `User` | 用户数据访问 |
| `KnowledgeMapper.java` | `Knowledge` | 知识点数据访问 |
| `QuestionMapper.java` | `Question` | 题目数据访问 |
| `QuestionRecordMapper.java` | `QuestionRecord` | 答题记录数据访问 |
| `UserMasteryMapper.java` | `UserMastery` | 掌握度数据访问 |
| `PromptTemplateMapper.java` | `PromptTemplate` | Prompt模板数据访问 |

---

### 5. 业务服务层 (service)

#### 5.1 Service类

| 文件 | 说明 |
|------|------|
| `AuthService.java` | 用户认证服务（登录、密码验证） |
| `InterviewService.java` | 面试业务服务（题目生成、答案提交、掌握度更新） |
| `KnowledgeService.java` | 知识树管理服务（初始化、查询、趋势计算） |

#### 5.2 DTO类 (dto)

| 文件 | 说明 |
|------|------|
| `LoginRequest.java` | 登录请求DTO |
| `LoginResponse.java` | 登录响应DTO |
| `InitKnowledgeTreeRequest.java` | 初始化知识树请求DTO |
| `KnowledgeNodeDTO.java` | 知识树节点DTO（用于前端展示） |
| `ProgressTrendDTO.java` | 进度趋势DTO |
| `QuestionDTO.java` | 题目DTO |
| `AnswerRequest.java` | 答案提交请求DTO |
| `AnswerResult.java` | 答案评分结果DTO |
| `FeedbackItem.java` | 反馈项DTO（AnswerResult的子项） |

---

### 6. AI能力层 (ai)

| 文件 | 说明 |
|------|------|
| `PromptService.java` | Prompt模板管理（渲染、变量替换） |
| `LlmClient.java` | LLM调用封装（LangChain4j封装） |

---

### 7. 接口层 (interfaces)

| 文件 | 说明 |
|------|------|
| `AuthController.java` | 认证接口（POST /api/login） |
| `InterviewController.java` | 面试接口（题目、提交答案、知识点列表） |
| `KnowledgeController.java` | 知识树接口（初始化、查询、趋势） |
| `TokenInterceptor.java` | Token认证拦截器 |

---

### 8. 配置层 (config)

| 文件 | 说明 |
|------|------|
| `WebConfig.java` | Web配置（拦截器注册） |
| `LlmConfig.java` | LLM配置（LangChain4j Bean配置） |
| `RedisConfig.java` | Redis配置（RedisTemplate Bean配置） |

---

### 9. 资源文件 (resources)

#### 9.1 配置文件

| 文件 | 说明 |
|------|------|
| `application.yml` | Spring Boot应用配置（数据库、Redis、LLM、MyBatis-Plus） |

#### 9.2 SQL脚本

| 文件 | 说明 |
|------|------|
| `schema.sql` | 数据库表结构定义（6个表） |
| `init-data.sql` | 初始化数据（Prompt模板、测试用户） |

#### 9.3 前端页面 (static)

| 文件 | 说明 |
|------|------|
| `interview.html` | 答题页面（登录、题目展示、答案提交、评分展示） |
| `progress.html` | 学习计划页面（知识树、进度、趋势图） |

---

## 📊 文件统计

### Java类文件统计
- **实体类**: 6个
- **Mapper接口**: 6个
- **Service类**: 3个
- **DTO类**: 9个
- **Controller类**: 3个
- **配置类**: 3个
- **AI层类**: 2个
- **拦截器**: 1个
- **主启动类**: 1个
- **总计**: 34个Java类

### 资源文件统计
- **配置文件**: 1个（application.yml）
- **SQL脚本**: 2个（schema.sql, init-data.sql）
- **前端页面**: 2个（interview.html, progress.html）

### 文档文件统计
- **README.md**: 项目简介
- **TECHNICAL_DESIGN.md**: 技术设计文档
- **CHANGELOG.md**: 更新日志
- **PROJECT_STRUCTURE.md**: 项目结构清单（本文件）

---

## 🔍 快速查找指南

### 按功能查找

#### 用户认证
- 实体: `domain/entity/User.java`
- Mapper: `infra/mapper/UserMapper.java`
- Service: `service/AuthService.java`
- Controller: `interfaces/AuthController.java`
- DTO: `service/dto/LoginRequest.java`, `LoginResponse.java`

#### 知识树管理
- 实体: `domain/entity/Knowledge.java`
- Mapper: `infra/mapper/KnowledgeMapper.java`
- Service: `service/KnowledgeService.java`
- Controller: `interfaces/KnowledgeController.java`
- DTO: `service/dto/KnowledgeNodeDTO.java`, `InitKnowledgeTreeRequest.java`

#### 题目和答题
- 实体: `domain/entity/Question.java`, `QuestionRecord.java`
- Mapper: `infra/mapper/QuestionMapper.java`, `QuestionRecordMapper.java`
- Service: `service/InterviewService.java`
- Controller: `interfaces/InterviewController.java`
- DTO: `service/dto/QuestionDTO.java`, `AnswerRequest.java`, `AnswerResult.java`

#### 掌握度跟踪
- 实体: `domain/entity/UserMastery.java`
- Mapper: `infra/mapper/UserMasteryMapper.java`
- Service: `service/InterviewService.java`, `KnowledgeService.java`
- DTO: `service/dto/ProgressTrendDTO.java`

#### AI集成
- Service: `ai/PromptService.java`, `ai/LlmClient.java`
- 实体: `domain/entity/PromptTemplate.java`
- Mapper: `infra/mapper/PromptTemplateMapper.java`

---

## 📝 文件创建顺序建议

如果从零开始重建项目，建议按以下顺序创建文件：

1. **配置和启动类**
   - `pom.xml`
   - `application.yml`
   - `QuizMeUpApplication.java`

2. **数据库脚本**
   - `schema.sql`
   - `init-data.sql`

3. **实体类**（6个）
   - 按依赖关系：User → Knowledge → Question → QuestionRecord → UserMastery → PromptTemplate

4. **Mapper接口**（6个）
   - 对应每个实体类

5. **DTO类**（9个）
   - 按功能模块分组创建

6. **AI层**
   - `PromptService.java`
   - `LlmClient.java`

7. **Service层**（3个）
   - `AuthService.java`
   - `KnowledgeService.java`
   - `InterviewService.java`

8. **Controller层**（3个）
   - `AuthController.java`
   - `KnowledgeController.java`
   - `InterviewController.java`

9. **配置类**（3个）
   - `RedisConfig.java`
   - `LlmConfig.java`
   - `WebConfig.java`

10. **拦截器**
    - `TokenInterceptor.java`

11. **前端页面**（2个）
    - `interview.html`
    - `progress.html`

---

**最后更新**: 2024-01-08
