# VertexFlow AI Spring Boot Starter 中文使用指南

本文档介绍如何在 Spring Boot 项目中使用 VertexFlow AI Starter。

---

## 1. Starter 能力

VertexFlow AI Spring Boot Starter 支持自动配置：

- ChatModel
- AiClient
- ChatMemory
- RagEngine
- VectorStore
- ToolRegistry
- SimpleToolAgent
- AiCallLogger

---

## 2. application.yml 配置

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

## 3. 配置说明

| 配置 | 说明 |
|---|---|
| vertexflow.ai.enabled | 是否启用 VertexFlow AI 自动配置 |
| vertexflow.ai.provider | 模型供应商，默认 openai-compatible |
| vertexflow.ai.api-key | API Key |
| vertexflow.ai.base-url | OpenAI-compatible API 地址 |
| vertexflow.ai.model | 模型名称 |
| vertexflow.ai.temperature | 温度参数 |
| vertexflow.ai.max-tokens | 最大输出 token |
| vertexflow.ai.console-log | 是否打印调用日志 |

---

## 4. Memory 配置

```yaml
vertexflow:
  ai:
    memory:
      enabled: true
      max-messages: 10
      conversation-id: spring-demo-user
```

| 配置 | 说明 |
|---|---|
| enabled | 是否启用记忆 |
| max-messages | 最大保留消息数 |
| conversation-id | 默认会话 ID |

---

## 5. RAG 配置

```yaml
vertexflow:
  ai:
    rag:
      enabled: true
      top-k: 3
      return-sources: true
      chunk-size: 300
      overlap: 50
```

| 配置 | 说明 |
|---|---|
| enabled | 是否启用 RAG |
| top-k | 检索返回片段数量 |
| return-sources | 是否返回来源 |
| chunk-size | 文档切片大小 |
| overlap | 文档切片重叠大小 |

---

## 6. Tool 配置

```yaml
vertexflow:
  ai:
    tool:
      enabled: true
      max-steps: 10
```

| 配置 | 说明 |
|---|---|
| enabled | 是否启用 Tool Agent |
| max-steps | Agent 最大执行步数 |

---

## 7. Controller 示例

```java
@RestController
public class ChatController {

    private final AiClient aiClient;

    public ChatController(AiClient aiClient) {
        this.aiClient = aiClient;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam("message") String message) {
        return aiClient.chat(message);
    }
}
```

---

## 8. RAG Controller 示例

```java
@RestController
public class RagController {

    private final RagEngine ragEngine;

    public RagController(RagEngine ragEngine) {
        this.ragEngine = ragEngine;

        this.ragEngine.addDocument(new Document("demo-doc", """
                VertexFlow AI Framework 支持 ChatModel、Memory、RAG、Tool Calling 和 Spring Boot Starter。
                """));
    }

    @GetMapping("/rag")
    public String rag(@RequestParam("question") String question) {
        RagAnswer answer = ragEngine.askWithSources(question);
        return answer.content();
    }
}
```

---

## 9. Spring Tool 示例

```java
@Component
public class SpringWeatherTool {

    @AiTool(name = "getWeather", description = "根据城市查询天气")
    public String getWeather(
            @ToolParam(value = "city", description = "城市名称") String city
    ) {
        return city + " 今天是晴天。";
    }
}
```

开启：

```yaml
vertexflow:
  ai:
    tool:
      enabled: true
```

Starter 会自动扫描 Spring Bean 中的 `@AiTool` 方法并注册到 `ToolRegistry`。

---

## 10. Agent Controller 示例

```java
@RestController
public class AgentController {

    private final SimpleToolAgent simpleToolAgent;

    public AgentController(SimpleToolAgent simpleToolAgent) {
        this.simpleToolAgent = simpleToolAgent;
    }

    @GetMapping("/agent")
    public String agent(@RequestParam("message") String message) {
        AgentResponse response = simpleToolAgent.run(message);
        return response.answer();
    }
}
```

---

## 11. 中文接口测试

### 聊天

```text
http://localhost:8080/chat?message=你好，用中文介绍一下VertexFlow AI Framework
```

### 记忆

第一次：

```text
http://localhost:8080/memory-chat?message=我叫牛国庆，我正在开发VertexFlow AI Framework
```

第二次：

```text
http://localhost:8080/memory-chat?message=我叫什么，我正在开发什么项目？
```

### RAG

```text
http://localhost:8080/rag?question=VertexFlow AI Framework支持哪些能力？
```

### Agent

```text
http://localhost:8080/agent?message=不要直接回答。你必须先调用getWeather工具，参数city=Beijing，然后把工具结果用中文告诉我。
```

---

## 12. 注意事项

不要把真实 API Key 提交到 Git 仓库。

推荐使用环境变量：

```yaml
api-key: ${DEEPSEEK_API_KEY}
```

PowerShell 设置环境变量：

```powershell
$env:DEEPSEEK_API_KEY="your_api_key"
```

---

## 13. 常见问题

### 1. AiClient Bean 找不到

检查自动装配文件：

```text
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

内容：

```text
com.vertexflow.ai.spring.boot.starter.VertexFlowAiAutoConfiguration
```

### 2. API Key 401

说明 Key 不正确或没有配置到运行环境。

### 3. @RequestParam 报参数名错误

写成：

```java
@RequestParam("message") String message
```

不要省略参数名。

### 4. 中文乱码

配置：

```yaml
server:
  servlet:
    encoding:
      charset: UTF-8
      enabled: true
      force: true
```