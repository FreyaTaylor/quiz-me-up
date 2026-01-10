# 更新日志

本文档记录项目的所有更新迭代。

---

## v1.0.0 (2024-01-08)

### 初始版本发布

#### 功能实现
- ✅ 用户认证系统（SHA256密码加密）
- ✅ 知识树管理（多层级树形结构）
- ✅ 智能题目生成（基于LLM）
- ✅ 答案评分系统（多维度反馈）
- ✅ 学习进度跟踪（按知识点聚合）
- ✅ 学习趋势分析（可视化图表）

#### 数据库设计
- ✅ 用户表（users）
- ✅ 知识点表（lc_knowledge）- 支持树形结构
- ✅ 题目表（lc_questions）
- ✅ 答题记录表（lc_question_record）
- ✅ 用户掌握度表（lc_user_mastery）- 复合主键
- ✅ Prompt模板表（lc_prompt_template）

#### API接口
- ✅ POST /api/login - 用户登录
- ✅ POST /api/knowledge/init-tree - 初始化知识树
- ✅ GET /api/knowledge/tree - 获取知识树
- ✅ GET /api/knowledge/progress/trend - 获取趋势数据
- ✅ GET /api/interview/topics - 获取知识点列表
- ✅ GET /api/interview/questions - 获取题目
- ✅ POST /api/interview/submit - 提交答案

#### 前端页面
- ✅ interview.html - 答题页面（登录、题目展示、答案提交）
- ✅ progress.html - 学习计划页面（知识树、进度、趋势图）

#### 技术架构
- ✅ Spring Boot 3.3.5
- ✅ MyBatis-Plus 3.5.7
- ✅ LangChain4j (OpenAI适配器)
- ✅ MySQL 8.0+
- ✅ Redis
- ✅ 原生HTML + JavaScript + Tailwind CSS

---

## v1.1.0 (2024-01-08)

### 新增功能
- ✅ 思维导图页面（mindmap.html）
- ✅ 节点编辑功能（可编辑节点名称、描述、重要程度）
- ✅ AI扩充子树功能（第二层级以下节点可调用AI生成子节点）
- ✅ 思维导图可视化展示（Canvas渲染，显示节点掌握度）

### 删除功能
- ❌ 学习进度页面（progress.html）
- ❌ 知识树初始化接口（POST /api/knowledge/init-tree）
- ❌ 进度趋势接口（GET /api/knowledge/progress/trend）

### 技术变更
- 🔧 新增 UpdateNodeRequest DTO（节点更新请求）
- 🔧 新增 ExpandSubtreeRequest DTO（AI扩充请求）
- 🔧 KnowledgeNodeDTO 新增 description 字段
- 🔧 新增 EXPAND_SUBTREE Prompt模板

### API变更
- ➕ PUT /api/knowledge/mindmap/node - 更新节点
- ➕ POST /api/knowledge/mindmap/expand - AI扩充子树
- ➖ DELETE POST /api/knowledge/init-tree - 删除知识树初始化接口
- ➖ DELETE GET /api/knowledge/progress/trend - 删除进度趋势接口

---

## 更新说明

每次更新时，请在此文档中添加新的版本记录，包括：
1. 版本号
2. 更新日期
3. 新增功能
4. 修复问题
5. 技术变更
6. 数据库变更

---

**格式示例**:

## v1.2.0 (YYYY-MM-DD)

### 新增功能
- ✅ 功能描述

### 修复问题
- 🐛 问题描述

### 技术变更
- 🔧 变更描述

### 数据库变更
- 📊 变更描述
