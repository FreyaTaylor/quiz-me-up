# AI面试模拟平台

基于Spring Boot + LangChain4j的智能面试练习系统。

## 快速开始

### 环境要求
- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 安装步骤

1. **克隆项目**
```bash
git clone <repository-url>
cd quiz-me-up
```

2. **配置数据库**
```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE interview_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 执行SQL脚本
mysql -u root -p interview_ai < src/main/resources/schema.sql
mysql -u root -p interview_ai < src/main/resources/init-data.sql
```

3. **配置应用**
编辑 `src/main/resources/application.yml`:
- 修改数据库连接信息
- 修改Redis连接信息
- 配置OpenAI API Key（或使用环境变量 `OPENAI_API_KEY`）

4. **运行应用**
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

5. **访问应用**
- 答题页面: http://localhost:8080/interview.html
- 学习计划: http://localhost:8080/progress.html

## 测试账号

默认测试用户（密码: 123456）:
- 用户名: `test`
- 用户名: `admin`

## 文档

- [技术设计文档](./TECHNICAL_DESIGN.md) - 完整的技术架构和实现细节（**核心文档**）
- [项目结构清单](./PROJECT_STRUCTURE.md) - 所有文件清单和快速查找指南
- [更新日志](./CHANGELOG.md) - 版本更新记录

> 💡 **提示**: 如需从零开始重建项目，请先阅读`TECHNICAL_DESIGN.md`中的"从零开始重建项目"章节。

## 功能特性

- ✅ 用户认证系统
- ✅ 智能知识树生成
- ✅ 个性化题目生成
- ✅ 多维度答案评分
- ✅ 学习进度跟踪
- ✅ 可视化趋势分析

## 技术栈

- Spring Boot 3.3.5
- MyBatis-Plus 3.5.7
- LangChain4j 0.34.0
- MySQL 8.0+
- Redis
- Tailwind CSS

## 许可证

MIT License
