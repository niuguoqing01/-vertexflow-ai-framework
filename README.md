# 🚀 VertexFlow AI Framework

> 一个面向 Java 开发者的轻量级 AI 应用开发框架。  
> 让 Java 开发者也能像使用 Python AI 框架一样，快速构建 Chat、RAG、Memory、Tool Calling、Agent 和 Spring Boot AI 应用。

<p align="center">
  <b>Java AI Application Framework · RAG · Agent · Tool Calling · Spring Boot Starter</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17+-orange" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.x-green" />
  <img src="https://img.shields.io/badge/AI-RAG%20%7C%20Agent%20%7C%20Tool%20Calling-blueviolet" />
  <img src="https://img.shields.io/badge/Status-Active-brightgreen" />
  <img src="https://img.shields.io/badge/Version-v0.4.x-blue" />
</p>

---

## ✨ 项目简介

**VertexFlow AI Framework** 是一个专为 Java 开发者设计的轻量级 AI 应用开发框架。

它不是简单封装一个大模型接口，而是围绕真实 AI 应用开发场景，逐步构建出一套完整的 Java AI 基础设施：

- 模型调用
- 流式输出
- Prompt 模板
- 多轮记忆
- RAG 检索增强生成
- 文档加载与切分
- Tool Calling 工具调用
- Simple Agent 智能体
- Spring Boot Starter 自动配置

目标是让 Java 开发者能够用熟悉的工程化方式，快速搭建 AI 助手、知识库问答、智能客服、Agent 工作流和企业级 AI 应用。

---

## 🎯 为什么做 VertexFlow AI？

目前很多 AI 框架主要围绕 Python 生态，但大量企业级系统仍然运行在 Java / Spring Boot 体系中。

VertexFlow AI 希望解决这些问题：

- Java 开发者接入 AI 成本高
- RAG、Memory、Tool Calling 需要重复造轮子
- AI 能力和 Spring Boot 工程体系结合不够自然
- 普通模型调用容易做，但真正工程化 AI 应用难
- 缺少一个轻量、清晰、可扩展的 Java AI 底层框架

所以 VertexFlow AI 的目标是：

> 让 Java 开发者用 Java 的方式，优雅地构建 AI 应用。

---

## 🔥 当前核心能力

| 能力 | 说明 |
|---|---|
| ChatModel | 统一模型调用抽象 |
| StreamingChatModel | 流式输出能力 |
| AiClient | 面向业务侧的高级客户端 |
| PromptTemplate | Prompt 模板渲染 |
| ChatMemory | 多轮对话记忆 |
| EmbeddingModel | Embedding 抽象 |
| VectorStore | 向量存储抽象 |
| RagEngine | 检索增强生成 |
| RagAnswer | RAG 引用来源返回 |
| DocumentLoader | 文档加载 |
| DirectoryDocumentLoader | 目录批量加载 |
| MarkdownDocumentSplitter | Markdown 结构化分片 |
| ToolRegistry | 工具注册中心 |
| ToolCallExecutor | 工具执行器 |
| SimpleToolAgent | 工具调用 Agent |
| AgentStep | Agent 执行轨迹 |
| Spring Boot Starter | 自动配置能力 |

---

## ⚡ 快速开始

### 1. 克隆项目

```bash
git clone https://gitee.com/crayon-old-and-small/vertexflow-ai-framework.git
cd vertexflow-ai-framework
```

### 2. 配置环境变量

PowerShell：

```powershell
$env:DEEPSEEK_API_KEY="your_api_key"
```

### 3. 编译项目

```bash
mvn clean package -DskipTests
```

### 4. 启动 Spring Boot 示例

```bash
mvn -pl vertexflow-ai-spring-boot-example -am spring-boot:run
```

也可以直接在 IDEA 运行：

```text
VertexFlowAiSpringExampleApplication
```

---

## ✅ AiClient 链式调用

更适合业务侧直接使用的高级客户端。

```java
ChatModel model = OpenAiCompatibleChatModel.builder()
        .apiKey("your_api_key")
        .baseUrl("https://api.deepseek.com/v1")
        .model("deepseek-chat")
        .build();

AiClient client = AiClient.builder()
        .chatModel(model)
        .system("你是 VertexFlow AI Framework 的中文 AI 助手，请优先使用中文回答。")
        .build();

String answer = client.chat("VertexFlow AI Framework 是什么？");
System.out.println(answer);
```

---

## ✅ StreamingChatModel 流式输出

支持类似 ChatGPT 的流式响应。

```java
client.stream("介绍一下 RAG", response -> {
    System.out.print(response.content());

    if (response.finished()) {
        System.out.println("\n[stream finished]");
    }
});
```

---

## ✅ PromptTemplate 模板渲染

```java
PromptTemplate template = new PromptTemplate("""
        你是一个 Java AI 框架助手。
        请用一句话解释：{topic}
        """);

String prompt = template.render(Map.of("topic", "RAG"));
System.out.println(prompt);
```

---

## ✅ ChatMemory 多轮记忆

```java
WindowChatMemory memory = WindowChatMemory.builder()
        .maxMessages(10)
        .build();

AiClient client = AiClient.builder()
        .chatModel(model)
        .memory(memory)
        .conversationId("user-001")
        .system("你是一个带记忆的 AI 助手。")
        .build();

client.chat("我叫牛国庆，我正在开发 VertexFlow AI Framework。");

String answer = client.chat("我叫什么？我正在开发什么？");
System.out.println(answer);
```

---

## ✅ RAG 检索增强生成

```java
RagEngine rag = RagBuilder.create()
        .chatModel(model)
        .embeddingModel(new SimpleTextEmbedding(256))
        .splitter(new MarkdownDocumentSplitter(300, 50))
        .options(RagOptions.defaults()
                .setTopK(3)
                .setReturnSources(true))
        .build();

rag.addDocument(new Document("doc1", """
        VertexFlow AI Framework 是一个面向 Java 开发者的轻量级 AI 应用开发框架。
        它支持 ChatModel、Memory、RAG、Tool Calling 和 Spring Boot Starter。
        """));

RagAnswer answer = rag.askWithSources("VertexFlow AI Framework 支持哪些能力？");

System.out.println(answer.content());

for (RagSource source : answer.sources()) {
    System.out.println(source.documentId());
    System.out.println(source.chunkId());
    System.out.println(source.score());
    System.out.println(source.content());
}
```

---

## ✅ DocumentLoader 文档加载

```java
Document document = TextFileDocumentLoader.loadFile(
        "docs/vertexflow-intro.txt"
);

rag.addDocument(document);
```

---

## ✅ DirectoryDocumentLoader 目录批量加载

```java
List<Document> documents = DirectoryDocumentLoader.loadDirectory("docs");

for (Document document : documents) {
    rag.addDocument(document);
}
```

默认支持：

- `.txt`
- `.md`

---

## ✅ MarkdownDocumentSplitter

支持按 Markdown 标题结构切分文档。

```java
DocumentSplitter splitter = new MarkdownDocumentSplitter(500, 80);

List<DocumentChunk> chunks = splitter.split(document);
```

---

## 🛠 Tool Calling 工具调用

VertexFlow AI 支持将普通 Java 方法注册成 AI 工具。

### 定义工具

```java
public class WeatherTool {

    @AiTool(name = "getWeather", description = "根据城市查询天气")
    public String getWeather(
            @ToolParam(value = "city", description = "城市名称") String city
    ) {
        return city + " 今天是晴天。";
    }

    @AiTool(name = "calculateSum", description = "计算两个整数之和")
    public Integer calculateSum(
            @ToolParam(value = "a", description = "第一个数字") Integer a,
            @ToolParam(value = "b", description = "第二个数字") Integer b
    ) {
        return a + b;
    }
}
```

### 注册并执行工具

```java
ToolRegistry registry = new ToolRegistry();
registry.register(new WeatherTool());

ToolResult result = registry.execute("getWeather", Map.of(
        "city", "北京"
));

System.out.println(result.content());
```

---

## 🤖 SimpleToolAgent 智能体

VertexFlow AI 已经具备基础 Agent 闭环：

```text
用户问题
  -> 模型判断是否需要工具
  -> 解析 tool_calls
  -> 执行 Java 工具
  -> 将工具结果交给模型
  -> 生成最终回答
```

### Agent 示例

```java
ToolRegistry registry = new ToolRegistry();
registry.register(new WeatherTool());

SimpleToolAgent agent = SimpleToolAgent.builder()
        .chatModel(model)
        .toolRegistry(registry)
        .toolCallParser(new OpenAiToolCallParser())
        .maxSteps(10)
        .build();

AgentResponse response = agent.run(
        "你必须调用 getWeather 工具查询北京天气，然后用中文回答我。"
);

System.out.println(response.answer());
```

---

## 🔍 AgentStep 执行轨迹

Agent 不只是返回答案，还能返回完整执行链路。

```java
for (AgentStep step : response.steps()) {
    System.out.println(step.type());
    System.out.println(step.name());
    System.out.println(step.content());
}
```

一次完整工具调用通常包含：

```text
USER_INPUT
MODEL_RESPONSE
TOOL_CALL
TOOL_RESULT
FINAL_ANSWER
```

这对调试 Agent、构建可视化执行面板非常重要。

---

## 🌱 Spring Boot Starter

VertexFlow AI 提供 Spring Boot Starter，支持自动配置：

- AiClient
- ChatModel
- ChatMemory
- RagEngine
- ToolRegistry
- SimpleToolAgent

### application.yml

```yaml
server:
  port: 8080
  servlet:
    encoding:
      charset: UTF-8
      enabled: true
      force: true

vertexflow:
  ai:
    enabled: true
    provider: openai-compatible
    api-key: ${DEEPSEEK_API_KEY}
    base-url: https://api.deepseek.com/v1
    model: deepseek-chat
    temperature: 0.7
    max-tokens: 2048
    console-log: true

    memory:
      enabled: true
      max-messages: 10
      conversation-id: spring-demo-user

    rag:
      enabled: true
      top-k: 3
      return-sources: true
      chunk-size: 300
      overlap: 50

    tool:
      enabled: true
      max-steps: 10
```

---

## 🧪 Spring Boot 中文测试接口

启动示例项目后可以直接访问：

### 中文聊天

```text
http://localhost:8080/chat?message=你好，用中文介绍一下VertexFlow AI Framework
```

### 中文记忆

第一次：

```text
http://localhost:8080/memory-chat?message=我叫牛国庆，我正在开发VertexFlow AI Framework
```

第二次：

```text
http://localhost:8080/memory-chat?message=我叫什么，我正在开发什么项目？
```

### 中文 RAG

```text
http://localhost:8080/rag?question=VertexFlow AI Framework支持哪些能力？
```

### 中文 Agent 工具调用

```text
http://localhost:8080/agent?message=不要直接回答。你必须先调用getWeather工具，参数city=Beijing，然后把工具结果用中文告诉我。
```

---

## 📦 模块结构

```text
vertexflow-ai-core
vertexflow-ai-memory
vertexflow-ai-rag
vertexflow-ai-model-openai
vertexflow-ai-examples
vertexflow-ai-spring-boot-starter
vertexflow-ai-spring-boot-example
```

| 模块 | 说明 |
|---|---|
| vertexflow-ai-core | 核心抽象，包含 ChatModel、AiClient、Tool、Agent、异常、日志 |
| vertexflow-ai-memory | 对话记忆实现 |
| vertexflow-ai-rag | RAG、DocumentLoader、VectorStore、Splitter |
| vertexflow-ai-model-openai | OpenAI-compatible 模型适配 |
| vertexflow-ai-examples | 普通 Java 示例 |
| vertexflow-ai-spring-boot-starter | Spring Boot Starter 自动配置 |
| vertexflow-ai-spring-boot-example | Spring Boot 示例项目 |

---

## 🏗 架构设计

```text
                    ┌────────────────────────────┐
                    │        Application          │
                    └──────────────┬─────────────┘
                                   │
                    ┌──────────────▼─────────────┐
                    │          AiClient           │
                    └──────────────┬─────────────┘
                                   │
          ┌────────────────────────┼────────────────────────┐
          │                        │                        │
┌─────────▼─────────┐    ┌─────────▼─────────┐    ┌─────────▼─────────┐
│     ChatModel      │    │     RagEngine     │    │  SimpleToolAgent  │
└─────────┬─────────┘    └─────────┬─────────┘    └─────────┬─────────┘
          │                        │                        │
┌─────────▼─────────┐    ┌─────────▼─────────┐    ┌─────────▼─────────┐
│ OpenAI Compatible │    │    VectorStore     │    │   ToolRegistry    │
└───────────────────┘    └─────────┬─────────┘    └─────────┬─────────┘
                                   │                        │
                         ┌─────────▼─────────┐    ┌─────────▼─────────┐
                         │  EmbeddingModel   │    │   Java @AiTool    │
                         └───────────────────┘    └───────────────────┘
```

---

## 🧭 版本路线

| 版本 | 能力 |
|---|---|
| v0.1.x | ChatModel、Prompt、Memory、RAG 基础能力 |
| v0.2.x | AiClient、ChatOptions、异常体系、日志体系、Memory 增强 |
| v0.3.x | Tool Calling、ToolRegistry、ToolAgent、AgentTrace |
| v0.4.x | Spring Boot Starter、自动配置、中文示例、配置提示 |

---

## 🗺 Roadmap

### 模型能力

- [x] ChatModel
- [x] StreamingChatModel
- [x] OpenAI-compatible 模型适配
- [ ] 更多模型供应商适配
- [ ] 多模型路由
- [ ] 模型降级策略

### RAG 能力

- [x] RagEngine
- [x] RagAnswer 引用来源
- [x] VectorStore 抽象
- [x] DocumentLoader
- [x] DirectoryDocumentLoader
- [x] MarkdownDocumentSplitter
- [ ] QdrantVectorStore
- [ ] MilvusVectorStore
- [ ] pgvector 支持
- [ ] PDFDocumentLoader
- [ ] URLDocumentLoader

### Agent 能力

- [x] ToolRegistry
- [x] ToolCallExecutor
- [x] SimpleToolAgent
- [x] AgentStep 执行轨迹
- [ ] ReActAgent
- [ ] PlanAgent
- [ ] 多步工具调用
- [ ] MCP 工具协议适配

### Spring Boot 能力

- [x] AiClient 自动配置
- [x] Memory 自动配置
- [x] RAG 自动配置
- [x] ToolRegistry 自动配置
- [x] SimpleToolAgent 自动配置
- [x] 配置元数据提示
- [ ] RedisChatMemory 自动配置
- [ ] JDBC ChatMemory 自动配置
- [ ] Qdrant 自动配置

---

## 🧠 项目定位

VertexFlow AI Framework 不是一个 Demo 项目，而是一个持续演进的 Java AI 底层框架。

它的核心方向是：

```text
Chat + Memory + RAG + Tool Calling + Agent + Spring Boot
```

希望让 Java 开发者可以更轻松地构建：

- AI 助手
- 企业知识库
- 智能客服
- 代码助手
- 工作流 Agent
- 私有化 AI 应用
- Spring Boot AI 应用服务

---

## 👨‍💻 作者

牛国庆  
AI 应用开发工程师  
专注于 Java AI 应用开发、RAG、Agent、Prompt Engineering 和大模型工程化落地。

代表项目：

```text
VertexFlow AI Framework
```

---

## ⭐ Star

如果你觉得这个项目有价值，欢迎 Star、Fork 和一起完善。

> Keep Building. Keep Shipping.