# VertexFlow AI Framework Version Roadmap

## v0.1.x：RAG 基础能力

目标：搭建 Java AI Framework 的基础底座。

- ChatModel
- StreamingChatModel
- PromptTemplate
- ChatMemory
- EmbeddingModel
- VectorStore
- RagEngine
- RagAnswer
- DocumentLoader
- DirectoryDocumentLoader
- MarkdownDocumentSplitter
- RagBuilder

## v0.2.x：工程化增强

目标：让框架更像可复用 SDK。

- AiClient Builder
- ChatOptions
- ChatResponse 增强
- TokenUsage
- AiException
- AiCallLogger
- MemoryOptions
- AiClient Memory

## v0.3.x：Tool Calling 与 Agent

目标：让框架具备 Agent 雏形。

- @AiTool
- @ToolParam
- ToolRegistry
- ToolSchemaGenerator
- ToolCall
- ToolCallExecutor
- OpenAiToolCallParser
- ToolChatModel
- SimpleToolAgent
- AgentStep
- AgentOptions
- AgentException
- ToolResult 错误传播

## v0.4.x：Spring Boot Starter

目标：让 Java / Spring Boot 用户开箱即用。

- Spring Boot Starter
- AiClient 自动配置
- ChatModel 自动配置
- Memory 自动配置
- RagEngine 自动配置
- ToolRegistry 自动配置
- SimpleToolAgent 自动配置
- 配置元数据提示
- 中文示例文档

## v0.5.x：真实向量库与 RAG 增强

计划：

- QdrantVectorStore
- PgVectorStore
- MilvusVectorStore
- PDFDocumentLoader
- URLDocumentLoader
- Markdown 元数据增强
- RAG 引用格式优化

## v0.6.x：Agent 增强

计划：

- ReActAgent
- PlanAgent
- 多轮工具调用
- Agent 记忆
- Agent Trace 可视化
- Agent 执行限流
- Agent 工具权限控制

## v0.7.x：企业级能力

计划：

- RedisChatMemory
- JdbcChatMemory
- Spring Boot Redis Memory 自动配置
- 调用监控
- Token 统计
- 多模型路由
- 模型降级策略

## v1.0.0：稳定版本

目标：

- API 稳定
- 文档完善
- Starter 可用
- RAG 可扩展
- Agent 可运行
- 工程结构清晰
