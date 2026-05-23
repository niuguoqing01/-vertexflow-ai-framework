# VertexFlow AI Tool Calling 与 Agent 中文使用指南

本文档介绍 VertexFlow AI Framework 中工具调用、工具注册、工具执行、Tool Schema、SimpleToolAgent 和 AgentStep 执行轨迹。

---

## 1. Tool Calling 是什么？

Tool Calling 的目标是让大模型可以调用 Java 方法。

基本流程：

```text
用户问题
  -> 模型判断需要工具
  -> 生成 tool_calls
  -> 框架解析工具调用
  -> 执行 Java 方法
  -> 将工具结果交给模型
  -> 生成最终答案
```

---

## 2. 定义工具

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

---

## 3. 注册工具

```java
ToolRegistry registry = new ToolRegistry();
registry.register(new WeatherTool());
```

查看已注册工具：

```java
for (ToolDefinition definition : registry.list()) {
    System.out.println(definition.name());
    System.out.println(definition.description());
    System.out.println(definition.parameters());
}
```

---

## 4. 执行工具

```java
ToolResult result = registry.execute("getWeather", Map.of(
        "city", "北京"
));

System.out.println(result.content());
```

---

## 5. ToolCall 抽象

```java
ToolCall toolCall = new ToolCall("getWeather", Map.of(
        "city", "北京"
));

ToolCallResult result = ToolCallExecutor.create(registry)
        .execute(toolCall);

System.out.println(result.result().content());
```

---

## 6. 批量执行工具

```java
List<ToolCall> calls = List.of(
        new ToolCall("getWeather", Map.of("city", "北京")),
        new ToolCall("calculateSum", Map.of("a", 10, "b", 20))
);

List<ToolCallResult> results = ToolCallExecutor.create(registry)
        .executeAll(calls);
```

---

## 7. 生成 OpenAI-compatible Tool Schema

```java
List<Map<String, Object>> schemas = registry.openAiToolSchemas();

System.out.println(schemas);
```

生成结构类似：

```json
{
  "type": "function",
  "function": {
    "name": "getWeather",
    "description": "根据城市查询天气",
    "parameters": {
      "type": "object",
      "properties": {
        "city": {
          "type": "string",
          "description": "城市名称"
        }
      },
      "required": ["city"]
    }
  }
}
```

---

## 8. OpenAiToolCallParser

将模型返回的 `tool_calls` 解析为框架内部的 `ToolCall`：

```java
OpenAiToolCallParser parser = new OpenAiToolCallParser();

List<ToolCall> toolCalls = parser.parse(response.rawResponse());
```

---

## 9. SimpleToolAgent

SimpleToolAgent 自动完成：

```text
用户问题
  -> 模型选择工具
  -> 执行工具
  -> 模型总结最终答案
```

示例：

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

## 10. AgentStep 执行轨迹

```java
for (AgentStep step : response.steps()) {
    System.out.println("type: " + step.type());
    System.out.println("name: " + step.name());
    System.out.println("content: " + step.content());
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

---

## 11. AgentOptions

```java
SimpleToolAgent agent = SimpleToolAgent.builder()
        .chatModel(model)
        .toolRegistry(registry)
        .toolCallParser(new OpenAiToolCallParser())
        .options(AgentOptions.builder()
                .maxSteps(10)
                .returnSteps(true)
                .build())
        .build();
```

---

## 12. 工具失败处理

```java
ToolResult result = registry.execute("failTool", Map.of(
        "reason", "testing"
));

System.out.println(result.success());
System.out.println(result.errorCode());
System.out.println(result.errorMessage());
```

工具失败不会一定打断 Agent，可以被封装为 `ToolResult.failure`，并进入 AgentStep。

---

## 13. 推荐实践

开发工具时建议：

- 工具名使用英文，例如 `getWeather`
- 工具描述写清楚用途
- 参数使用 `@ToolParam`
- 返回值尽量简单清晰
- 工具内部异常要明确
- Agent 设置合理 `maxSteps`

推荐提示词：

```text
不要直接回答。你必须先调用 getWeather 工具，参数 city=Beijing，然后把工具结果用中文告诉我。
```