# 知忆岛 - AI模块技术文档

## 1. 概述

知忆岛的AI模块基于**硅基流动（SiliconFlow）API**实现，调用多种大语言模型为用户提供智能摘要、关键词提取、知识图谱生成、向量搜索等功能。

---

## 2. 技术架构

### 2.1 架构图

```
用户请求 → Spring Boot → AiController → AiServiceImpl → AiProviderServiceImpl → 硅基流动API → 大语言模型
                                                              ↓
                                                    Embeddings API → 向量数据库
```

### 2.2 核心组件

| 组件 | 说明 |
|------|------|
| **AiController** | AI功能HTTP接口控制器 |
| **AiService** | AI业务服务接口 |
| **AiServiceImpl** | AI业务服务实现 |
| **AiProviderService** | AI提供商服务接口（抽象层） |
| **AiProviderServiceImpl** | 硅基流动API调用实现 |

### 2.3 使用的模型

| 模型 | 用途 | 说明 |
|------|------|------|
| Pro/zai-org/GLM-5 | 对话生成 | 主要对话模型 |
| Qwen/Qwen2.5-32B-Instruct | 知识提取 | 复杂推理任务 |
| BAAI/bge-large-zh-v1.5 | 向量嵌入 | 中文语义向量 |

### 2.4 依赖配置

无需额外依赖，使用Spring内置的RestTemplate调用API。

---

## 3. 配置说明

### 3.1 application.yml配置

```yaml
spring:
  ai:
    siliconflow:
      base-url: https://api.siliconflow.cn/v1
      api-key: your-api-key
      model: Pro/zai-org/GLM-5              # 主对话模型
      knowledge-model: Qwen/Qwen2.5-32B-Instruct  # 知识提取模型
      embedding-model: BAAI/bge-large-zh-v1.5     # 向量嵌入模型
      options:
        temperature: 0.7
        max-tokens: 4096
```

### 3.2 参数说明

| 参数 | 说明 | 默认值 |
|------|------|--------|
| base-url | 硅基流动API地址 | https://api.siliconflow.cn/v1 |
| api-key | API密钥 | 必填 |
| model | 主对话模型 | Pro/zai-org/GLM-5 |
| knowledge-model | 知识提取模型 | Qwen/Qwen2.5-32B-Instruct |
| embedding-model | 向量嵌入模型 | BAAI/bge-large-zh-v1.5 |
| temperature | 生成温度 | 0.7 |
| max-tokens | 最大输出token数 | 4096 |

---

## 4. 功能模块

### 4.1 AI摘要生成

**接口**：`POST /api/ai/summary`

**流程**：
```
1. 用户提交内容
2. 构建提示词（根据风格和长度参数）
3. 调用硅基流动API生成摘要
4. 保存到数据库
5. 发送通知
6. 返回摘要结果
```

**参数说明**：

| 参数 | 类型 | 说明 |
|------|------|------|
| content | String | 待摘要的内容（必填） |
| title | String | 摘要标题（可选） |
| style | String | 摘要风格：keypoints（要点式）/ narrative（叙述式） |
| length | String | 摘要长度：short（100字）/ medium（200-300字）/ detailed（300-500字） |

---

### 4.2 关键词提取

**接口**：`POST /api/keywords`

**流程**：
```
1. 用户提交文本
2. 构建提示词
3. 调用硅基流动API提取关键词
4. 返回关键词字符串
```

---

### 4.3 知识图谱生成

**接口**：`GET /api/ai/knowledge-graph`

**流程**：
```
1. 查询用户所有笔记和知识库
2. 构建包含用户数据的提示词
3. 要求AI返回JSON格式的图谱数据
4. 清理返回结果（去除markdown代码块标记）
5. 返回JSON字符串供前端渲染
```

**返回格式**：
```json
{
  "nodes": [
    {"id": 1, "label": "概念名称", "level": 0, "color": "#667eea"}
  ],
  "edges": [
    {"from": 1, "to": 2}
  ]
}
```

---

### 4.4 AI问答对话

**接口**：`POST /api/ai/chat`

**功能**：基于用户笔记和知识库内容回答问题

**流程**：
```
1. 获取用户所有笔记和知识库
2. 用问题进行语义搜索
3. 构建上下文（匹配到的相关内容）
4. 调用AI生成回答
5. 返回答案
```

**请求参数**：

| 参数 | 类型 | 说明 |
|------|------|------|
| question | String | 用户问题 |
| context | String | 当前上下文（可选） |

---

### 4.5 AI智能搜索

**接口**：通过 `SearchController` 调用

**功能**：语义扩展搜索 + 向量搜索

**流程**：
```
1. 语义扩展：让AI生成相关搜索词
2. 关键词搜索：用扩展词搜索笔记和知识库
3. 向量搜索：生成查询向量，进行相似度匹配
4. 合并去重结果
5. 返回搜索结果
```

---

### 4.6 AI语义化问答

**功能**：理解用户自然语言问题，基于数据库内容生成针对性回答

**流程**：
```
1. 意图识别（定义/如何/为什么/对比/例子/搜索/通用）
2. 关键词提取
3. 语义搜索 + 向量搜索
4. 构建提示词，包含相关内容
5. AI生成回答
6. 标注来源类型（知识库/通用知识）
```

---

### 4.7 知识提取

**功能**：从笔记内容中提取知识点，建议知识库分类

**流程**：
```
1. 构建知识提取提示词
2. 调用知识提取模型（Qwen/Qwen2.5-32B-Instruct）
3. 解析JSON响应
4. 返回知识标题、分类、知识点列表
```

**返回格式**：
```json
{
  "knowledgeTitle": "知识库标题",
  "knowledgeCategory": "分类",
  "knowledgePoints": [
    {"title": "知识点标题", "content": "知识点内容"}
  ]
}
```

**分类选项**：技术、学习、工作、生活、健康、财经、文化、其他

---

### 4.8 向量搜索

**功能**：基于语义相似度搜索笔记

**流程**：
```
1. 生成查询文本的向量嵌入
2. 在数据库中进行向量相似度计算
3. 返回相似度最高的笔记
```

**相关方法**：

| 方法 | 说明 |
|------|------|
| generateEmbedding(text) | 生成文本向量 |
| searchByVector(userId, query, limit) | 向量搜索 |
| searchByVectorWithThreshold(userId, query, threshold, limit) | 带相似度阈值的向量搜索 |
| batchGenerateEmbeddings(userId, batchSize) | 批量生成笔记向量 |

---

### 4.9 保存摘要到笔记

**接口**：`POST /api/ai/summary/{id}/save-to-note`

**流程**：
```
1. 根据摘要ID查询摘要记录
2. 验证用户权限
3. 创建新笔记，内容为摘要
4. 分类标记为"其他"
5. 保存到笔记表
```

---

## 5. 数据流图

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   前端      │────→│  AiController │────→│ AiServiceImpl│
└─────────────┘     └──────────────┘     └─────────────┘
                                                │
                    ┌───────────────────────────┼───────────────────────────┐
                    ↓                           ↓                           ↓
           ┌──────────────────┐       ┌──────────────┐           ┌──────────────┐
           │AiProviderServiceImpl│     │AiSummaryMapper│           │  NoteMapper  │
           └──────────────────┘       └──────────────┘           └──────────────┘
                    │
                    ↓
           ┌──────────────────┐
           │  硅基流动 API    │
           │ (api.siliconflow)│
           └──────────────────┘
                    │
          ┌────────┴────────┐
          ↓                 ↓
   ┌────────────┐    ┌────────────┐
   │ Chat API   │    │Embedding API│
   │ (对话生成) │    │ (向量嵌入)  │
   └────────────┘    └────────────┘
```

---

## 6. API接口汇总

| 接口 | 方法 | 功能 |
|------|------|------|
| /api/ai/summary | POST | 生成AI摘要 |
| /api/ai/summary/history | GET | 获取摘要历史 |
| /api/ai/summary/{id} | DELETE | 删除摘要 |
| /api/ai/summary/{id}/save-to-note | POST | 保存摘要到笔记 |
| /api/ai/keywords | POST | 提取关键词 |
| /api/ai/knowledge-graph | GET | 生成知识图谱 |
| /api/ai/chat | POST | AI问答对话 |

---

## 7. 错误处理

### 7.1 常见错误

| 错误信息 | 原因 | 解决方案 |
|----------|------|----------|
| AI服务暂时不可用 | API调用失败 | 检查API密钥和网络连接 |
| 连接失败 | 网络问题 | 检查网络连接 |
| 权限不足 | API密钥无效 | 检查API密钥是否正确 |
| 请求过于频繁 | 触发限流 | 稍后重试 |

### 7.2 异常处理代码

```java
try {
    summaryContent = aiProviderService.generateResponse(prompt);
} catch (Exception e) {
    log.error("调用AI服务失败：{}", e.getMessage());
    throw new BusinessException(500, "AI服务暂时不可用，请稍后再试");
}
```

### 7.3 错误消息翻译

系统会将英文错误信息翻译为中文：
- connection refused → 连接失败
- timeout → 连接超时
- unauthorized → 权限不足
- too many requests → 请求过于频繁

---

## 8. 性能优化建议

### 8.1 模型选择

| 场景 | 推荐模型 | 说明 |
|------|----------|------|
| 日常对话 | GLM-5 | 平衡性能和成本 |
| 复杂推理 | Qwen2.5-32B | 更强的推理能力 |
| 向量嵌入 | bge-large-zh-v1.5 | 中文语义效果好 |

### 8.2 优化建议

1. **批量处理**：批量生成向量嵌入，减少API调用次数
2. **缓存结果**：对相同内容的摘要结果进行缓存
3. **异步处理**：使用@Async异步生成向量
4. **阈值过滤**：向量搜索时设置相似度阈值，过滤低质量结果

---

## 9. 优势与特点

### 9.1 优势

- **云端服务**：无需本地部署模型，降低硬件要求
- **多模型支持**：可根据任务选择不同模型
- **向量搜索**：支持语义相似度搜索，提高搜索精度
- **智能问答**：基于用户知识库进行针对性回答

### 9.2 注意事项

- 需要有效的API密钥
- 调用次数受API套餐限制
- 网络连接要求稳定
- 向量嵌入会占用数据库存储空间

---

## 版本信息

- 文档版本: 2.0.0
- 更新日期: 2026-05-03
- AI服务提供商: 硅基流动（SiliconFlow）
- 主模型: Pro/zai-org/GLM-5
