# VertexFlow AI Chat 中文使用指南

本文档介绍 VertexFlow AI Framework 中 ChatModel、AiClient、StreamingChatModel、PromptTemplate 和 ChatMemory 的使用方式。

---

## 1. ChatModel 基础调用

`ChatModel` 是 VertexFlow AI 的基础模型调用抽象。

```java
ChatModel model = OpenAiCompatibleChatModel.builder()
        .apiKey("your_api_key")
        .baseUrl("https://api.deepseek.com/v1")
        .model("deepseek-chat")
        .build();

ChatRequest request = new ChatRequest()
        .addMessage(ChatMessage.system("你是一个中文 AI 助手。"))
        .addMessage(ChatMessage.user("你好，介绍一下 VertexFlow AI Framework"));

ChatResponse response = model.call(request);

System.out.println(response.content());
```

---

## 2. AiClient 链式调用

`AiClient` 是业务侧更推荐使用的高级客户端。

```java
AiClient client = AiClient.builder()
        .chatModel(model)
        .system("你是 VertexFlow AI Framework 的中文 AI 助手。")
        .build();

String answer = client.chat("VertexFlow AI Framework 是什么？");

System.out.println(answer);
```

---

## 3. 流式输出

```java
client.stream("请用中文介绍一下 RAG", response -> {
    System.out.print(response.content());

    if (response.finished()) {
        System.out.println("\n[stream finished]");
    }
});
```

---

## 4. PromptTemplate 模板

```java
PromptTemplate template = new PromptTemplate("""
        你是一个 Java AI 框架助手。
        请用一句话解释：{topic}
        """);

String prompt = template.render(Map.of("topic", "RAG"));

System.out.println(prompt);
```

---

## 5. ChatMemory 对话记忆

```java
WindowChatMemory memory = WindowChatMemory.builder()
        .maxMessages(10)
        .build();

AiClient client = AiClient.builder()
        .chatModel(model)
        .memory(memory)
        .conversationId("user-001")
        .system("你是一个带记忆能力的中文 AI 助手。")
        .build();

client.chat("我叫牛国庆，我正在开发 VertexFlow AI Framework。");

String answer = client.chat("我叫什么？我正在开发什么？");

System.out.println(answer);
```

---

## 6. ChatOptions 参数配置

```java
ChatRequest request = new ChatRequest()
        .setOptions(ChatOptions.builder()
                .temperature(0.2)
                .maxTokens(300)
                .topP(0.9)
                .build())
        .addMessage(ChatMessage.user("用一句话解释 ChatOptions"));

ChatResponse response = model.call(request);
```

---

## 7. ChatResponse 响应信息

```java
ChatResponse response = model.call(request);

System.out.println(response.content());
System.out.println(response.model());
System.out.println(response.finishReason());

if (response.usage() != null) {
    System.out.println(response.usage().inputTokens());
    System.out.println(response.usage().outputTokens());
    System.out.println(response.usage().totalTokens());
}
```

---

## 8. 推荐用法

业务开发时推荐优先使用：

```java
AiClient client = AiClient.builder()
        .chatModel(model)
        .system("你是中文 AI 助手。")
        .build();
```

底层框架扩展时再使用：

```java
ChatModel
ChatRequest
ChatResponse
```