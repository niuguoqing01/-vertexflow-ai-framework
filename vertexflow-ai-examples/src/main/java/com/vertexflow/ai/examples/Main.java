package com.vertexflow.ai.examples;

import com.vertexflow.ai.core.chat.AiClient;
import com.vertexflow.ai.core.chat.ChatMessage;
import com.vertexflow.ai.core.chat.ChatModel;
import com.vertexflow.ai.core.prompt.PromptTemplate;
import com.vertexflow.ai.core.tool.*;
import com.vertexflow.ai.memory.WindowChatMemory;
import com.vertexflow.ai.model.openai.OpenAiCompatibleChatModel;
import com.vertexflow.ai.rag.Document;
import com.vertexflow.ai.rag.RagEngine;
import com.vertexflow.ai.core.chat.StreamingChatModel;
import com.vertexflow.ai.core.embedding.EmbeddingModel;
import com.vertexflow.ai.core.embedding.EmbeddingResponse;
import com.vertexflow.ai.model.openai.OpenAiCompatibleEmbeddingModel;
import com.vertexflow.ai.rag.SimpleTextEmbedding;
import com.vertexflow.ai.rag.InMemoryVectorStore;
import com.vertexflow.ai.rag.SimpleTextEmbedding;
import com.vertexflow.ai.rag.VectorSearchResult;
import com.vertexflow.ai.rag.VectorStore;
import com.vertexflow.ai.rag.DocumentSplitter;
import com.vertexflow.ai.rag.RagAnswer;
import com.vertexflow.ai.rag.RagOptions;
import com.vertexflow.ai.rag.RagSource;
import com.vertexflow.ai.rag.TextFileDocumentLoader;
import com.vertexflow.ai.rag.DirectoryDocumentLoader;
import com.vertexflow.ai.rag.FixedSizeDocumentSplitter;
import com.vertexflow.ai.rag.DocumentSplitter;
import com.vertexflow.ai.rag.DocumentChunk;
import com.vertexflow.ai.rag.MarkdownDocumentSplitter;
import com.vertexflow.ai.rag.RagBuilder;
import com.vertexflow.ai.core.VertexFlowAi;
import com.vertexflow.ai.core.chat.ChatOptions;
import com.vertexflow.ai.core.chat.ChatRequest;
import com.vertexflow.ai.core.chat.ChatResponse;
import com.vertexflow.ai.core.exception.AiException;
import com.vertexflow.ai.core.log.ConsoleAiCallLogger;
import com.vertexflow.ai.core.memory.MemoryOptions;
import com.vertexflow.ai.model.openai.OpenAiToolCallParser;
import com.vertexflow.ai.core.tool.SimpleToolAgent;
import com.vertexflow.ai.core.tool.AgentResponse;
import com.vertexflow.ai.core.tool.AgentStep;
import com.vertexflow.ai.core.tool.AgentOptions;
import com.vertexflow.ai.core.exception.AgentException;
import com.vertexflow.ai.rag.QdrantVectorStore;
import com.vertexflow.ai.rag.PdfDocumentLoader;
import com.vertexflow.ai.rag.UrlDocumentLoader;
import com.vertexflow.ai.rag.AddDocumentResult;
import com.vertexflow.ai.core.tool.ReActAgent;
import com.vertexflow.ai.core.tool.ReActAgentOptions;

import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        System.out.println("====== VertexFlow AI Framework v0.1 started ======");

        testPrompt();
        testMemory();

        String apiKey = System.getenv("DEEPSEEK_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            System.out.println();
            System.out.println("DEEPSEEK_API_KEY not found. Skip real model call.");
            System.out.println("PowerShell example:");
            System.out.println("$env:DEEPSEEK_API_KEY=\"your_api_key\"");
            return;
        }

        ChatModel model = OpenAiCompatibleChatModel.builder()
                .apiKey(apiKey)
                .baseUrl("https://api.deepseek.com/v1")
                .model("deepseek-chat")
                .callLogger(new ConsoleAiCallLogger())
                .build();
        EmbeddingModel embeddingModel = OpenAiCompatibleEmbeddingModel.builder()
                .apiKey(apiKey)
                .baseUrl("https://api.deepseek.com/v1")
                .model("text-embedding-v1")
                .callLogger(new ConsoleAiCallLogger())
                .build();
        testChat(model);
        testStream(model);
        testEmbedding(new SimpleTextEmbedding(256));
        testVectorStore();
        testRag(model);
        testRagWithSources(model);
        testDocumentLoader(model);
        testDirectoryDocumentLoader(model);
        testDocumentSplitter();
        testMarkdownDocumentSplitter();
        testRagWithMarkdownSplitter(model);
        testRagBuilder(model);
        testAiClientBuilder(model);
        testAiClientStreaming(model);
        testChatOptions(model);
        testChatResponse(model);
        testAiException();
        testAiClientMemory(model);
        testMemoryOptions();
        testToolCalling();
        testToolSchema();
        testToolCallExecutor();
        testOpenAiToolCallParser();
        testToolChatModel(model);
        testSimpleToolAgent(model);
        testSimpleToolAgentTrace(model);
        testSimpleToolAgentOptions(model);
        testAgentException(model);
        testToolFailurePropagation();
        testQdrantVectorStore(model);
        testPdfDocumentLoader(model);
        testUrlDocumentLoader(model);
        testAddDocumentResult(model);
        testDeleteDocument(model);
        testReActAgent(model);
        testReActAgentJsonInput(model);
        testReActAgentFormatRecovery(model);
        testReActAgentFailureReason(model);
        testReActAgentToolNotFound(model);
        testReActAgentFailureInference(model);
        testReActScratchpad(model);
        testReActScratchpadJsonRender();
    }

    private static void testPrompt() {
        System.out.println();
        System.out.println("[1] PromptTemplate test");

        PromptTemplate template = PromptTemplate.from("""
                You are a {role}.
                Please explain this topic in one sentence: {topic}
                """);

        String prompt = template.render(Map.of(
                "role", "Java AI framework assistant",
                "topic", "RAG"
        ));

        System.out.println(prompt);
    }

    private static void testMemory() {
        System.out.println();
        System.out.println("[2] WindowChatMemory test");

        WindowChatMemory memory = new WindowChatMemory(3);
        memory.add("u1", ChatMessage.user("My name is Guoqing Niu"));
        memory.add("u1", ChatMessage.assistant("OK, I remember it"));
        memory.add("u1", ChatMessage.user("I am building VertexFlow AI Framework"));
        memory.add("u1", ChatMessage.assistant("It is a Java AI framework"));

        memory.get("u1").forEach(System.out::println);
    }

    private static void testChat(ChatModel model) {
        System.out.println();
        System.out.println("[3] AiClient real model test");

        AiClient client = AiClient.create(model)
                .system("You are the technical assistant of VertexFlow AI Framework. Answer briefly.");

        String answer = client.chat("Introduce VertexFlow AI Framework in one sentence.");
        System.out.println(answer);
    }

    private static void testRag(ChatModel model) {
        System.out.println();
        System.out.println("[7] RagEngine test");

        RagEngine rag = new RagEngine(model);

        rag.addDocument(new Document("doc1", """
                VertexFlow AI Framework is a lightweight AI application development framework for Java developers.
                It provides unified model calling, prompt templates, chat memory, RAG, and tool calling.
                The first version aims to help Java developers build AI applications like using LangChain in Python.
                """));

        String answer = rag.ask("What is VertexFlow AI Framework?");
        System.out.println(answer);
    }
    private static void testStream(ChatModel model) {
        System.out.println();
        System.out.println("[4] StreamingChatModel test");

        if (!(model instanceof StreamingChatModel streamingModel)) {
            System.out.println("Current model does not support streaming.");
            return;
        }

        streamingModel.stream("Introduce RAG in one short paragraph.", response -> {
            System.out.print(response.content());

            if (response.finished()) {
                System.out.println();
                System.out.println("[stream finished]");
            }
        });
    }

    private static void testEmbedding(EmbeddingModel embeddingModel) {
        System.out.println();
        System.out.println("[5] EmbeddingModel test");

        EmbeddingResponse response = embeddingModel.embed("VertexFlow AI Framework is a Java AI framework.");

        System.out.println("model: " + response.model());
        System.out.println("tokens: " + response.tokens());
        System.out.println("dimension: " + response.vector().length);
        System.out.println("first value: " + response.vector()[0]);
    }

    private static void testVectorStore() {
        System.out.println();
        System.out.println("[6] VectorStore test");

        VectorStore vectorStore = new InMemoryVectorStore(new SimpleTextEmbedding(256));

        Document document = new Document("doc-vector-1", """
            VertexFlow AI Framework provides ChatModel, StreamingChatModel, EmbeddingModel and VectorStore.
            VectorStore is used to store and search document chunks by vector similarity.
            """);

        DocumentSplitter splitter = new FixedSizeDocumentSplitter(100, 20);
        vectorStore.add(splitter.split(document));

        for (VectorSearchResult result : vectorStore.search("What is VectorStore?", 3)) {
            System.out.println("score: " + result.score());
            System.out.println("chunk: " + result.chunk().content());
        }
    }

    private static void testRagWithSources(ChatModel model) {
        System.out.println();
        System.out.println("[8] RagEngine sources test");

        RagOptions options = RagOptions.defaults()
                .setTopK(2)
                .setReturnSources(true);

        RagEngine rag = new RagEngine(
                model,
                new InMemoryVectorStore(new SimpleTextEmbedding(256)),
                options
        );

        rag.addDocument(new Document("doc-source-1", """
            VertexFlow AI Framework provides ChatModel, StreamingChatModel, EmbeddingModel and VectorStore.
            It is designed for Java developers who want to build AI applications with a simple framework API.
            """));

        rag.addDocument(new Document("doc-source-2", """
            RagEngine uses VectorStore to retrieve relevant document chunks.
            It then builds context and calls ChatModel to generate a grounded answer.
            """));

        RagAnswer answer = rag.askWithSources("How does RagEngine work?");

        System.out.println("answer:");
        System.out.println(answer.content());

        System.out.println();
        System.out.println("sources:");
        for (RagSource source : answer.sources()) {
            System.out.println("- documentId: " + source.documentId());
            System.out.println("  chunkId: " + source.chunkId());
            System.out.println("  score: " + source.score());
            System.out.println("  content: " + source.content());
        }
    }

    private static void testDocumentLoader(ChatModel model) {
        System.out.println();
        System.out.println("[9] DocumentLoader test");

        RagEngine rag = new RagEngine(model);

        Document document = TextFileDocumentLoader.loadFile(
                "vertexflow-ai-examples/src/main/resources/doc/vertexflow-intro.txt"
        );

        rag.addDocument(document);

        RagAnswer answer = rag.askWithSources("What capabilities does VertexFlow AI Framework provide?");

        System.out.println("answer:");
        System.out.println(answer.content());

        System.out.println();
        System.out.println("sources:");
        for (RagSource source : answer.sources()) {
            System.out.println("- documentId: " + source.documentId());
            System.out.println("  score: " + source.score());
            System.out.println("  content: " + source.content());
        }
    }

    private static void testDirectoryDocumentLoader(ChatModel model) {
        System.out.println();
        System.out.println("[10] DirectoryDocumentLoader test");

        RagEngine rag = new RagEngine(model);

        List<Document> documents = DirectoryDocumentLoader.loadDirectory(
                "vertexflow-ai-examples/src/main/resources/doc"
        );

        System.out.println("loaded documents: " + documents.size());

        for (Document document : documents) {
            System.out.println("- " + document.id());
            rag.addDocument(document);
        }

        RagAnswer answer = rag.askWithSources("How does VertexFlow AI Framework use RAG?");

        System.out.println();
        System.out.println("answer:");
        System.out.println(answer.content());

        System.out.println();
        System.out.println("sources:");
        for (RagSource source : answer.sources()) {
            System.out.println("- documentId: " + source.documentId());
            System.out.println("  score: " + source.score());
            System.out.println("  content: " + source.content());
        }
    }

    private static void testDocumentSplitter() {
        System.out.println();
        System.out.println("[11] DocumentSplitter test");

        DocumentSplitter splitter = new FixedSizeDocumentSplitter(80, 20);

        Document document = new Document("splitter-doc-1", """
            VertexFlow AI Framework supports pluggable document splitters.
            FixedSizeDocumentSplitter splits text by fixed character length with overlap.
            In the future, MarkdownDocumentSplitter and TokenDocumentSplitter can be added.
            """);

        for (DocumentChunk chunk : splitter.split(document)) {
            System.out.println("- chunkId: " + chunk.id());
            System.out.println("  content: " + chunk.content());
        }
    }

    private static void testMarkdownDocumentSplitter() {
        System.out.println();
        System.out.println("[12] MarkdownDocumentSplitter test");

        DocumentSplitter splitter = new MarkdownDocumentSplitter(200, 40);

        Document document = new Document("markdown-doc-1", """
            # VertexFlow AI Framework

            VertexFlow AI Framework is a lightweight AI application development framework for Java developers.

            ## ChatModel

            ChatModel provides unified model calling for OpenAI-compatible APIs, DeepSeek, Qwen and other LLM providers.

            ## StreamingChatModel

            StreamingChatModel provides streaming output, allowing AI responses to be printed chunk by chunk.

            ## RAG

            RagEngine loads documents, splits them into chunks, stores chunks in VectorStore, retrieves relevant chunks, and calls ChatModel to generate grounded answers.

            ### Sources

            RagAnswer can return answer content, context and source chunks with documentId, chunkId and score.
            """);

        for (DocumentChunk chunk : splitter.split(document)) {
            System.out.println("- chunkId: " + chunk.id());
            System.out.println("  content:");
            System.out.println(chunk.content());
            System.out.println();
        }
    }

    private static void testRagWithMarkdownSplitter(ChatModel model) {
        System.out.println();
        System.out.println("[13] RagEngine Markdown splitter test");

        VectorStore vectorStore = new InMemoryVectorStore(new SimpleTextEmbedding(256));

        RagEngine rag = new RagEngine(
                model,
                vectorStore,
                new MarkdownDocumentSplitter(300, 50),
                RagOptions.defaults().setTopK(2)
        );

        rag.addDocument(new Document("markdown-rag-doc", """
            # VertexFlow AI Framework

            VertexFlow AI Framework is a lightweight AI application development framework for Java developers.

            ## RAG

            RagEngine loads documents, splits them into chunks, stores chunks in VectorStore, retrieves relevant chunks, and calls ChatModel to generate grounded answers.

            ## VectorStore

            VectorStore is used to store document chunk vectors and search relevant chunks by similarity.

            ## DocumentSplitter

            DocumentSplitter is used to split documents into smaller chunks before embedding and retrieval.
            """));

        RagAnswer answer = rag.askWithSources("How does RagEngine work with Markdown documents?");

        System.out.println("answer:");
        System.out.println(answer.content());

        System.out.println();
        System.out.println("sources:");
        for (RagSource source : answer.sources()) {
            System.out.println("- documentId: " + source.documentId());
            System.out.println("  chunkId: " + source.chunkId());
            System.out.println("  score: " + source.score());
            System.out.println("  content: " + source.content());
        }
    }

    private static void testRagBuilder(ChatModel model) {
        System.out.println();
        System.out.println("[14] RagBuilder test");

        RagEngine rag = RagBuilder.create()
                .chatModel(model)
                .embeddingModel(new SimpleTextEmbedding(256))
                .splitter(new MarkdownDocumentSplitter(300, 50))
                .options(RagOptions.defaults().setTopK(2).setReturnSources(true))
                .build();

        rag.addDocument(new Document("builder-doc-1", """
            # VertexFlow AI Framework

            VertexFlow AI Framework is a lightweight AI framework for Java developers.

            ## Builder API

            RagBuilder provides a fluent API for creating RagEngine.
            It allows developers to configure ChatModel, EmbeddingModel, VectorStore, DocumentSplitter and RagOptions.

            ## Goal

            The goal is to make Java AI development simple, clean and framework-like.
            """));

        RagAnswer answer = rag.askWithSources("What does RagBuilder provide?");

        System.out.println("answer:");
        System.out.println(answer.content());

        System.out.println();
        System.out.println("sources:");
        for (RagSource source : answer.sources()) {
            System.out.println("- documentId: " + source.documentId());
            System.out.println("  chunkId: " + source.chunkId());
            System.out.println("  score: " + source.score());
            System.out.println("  content: " + source.content());
        }
    }

    private static void testAiClientBuilder(ChatModel model) {
        System.out.println();
        System.out.println("[15] AiClient Builder test");

        AiClient client = AiClient.builder()
                .chatModel(model)
                .system("You are the technical assistant of VertexFlow AI Framework. Answer briefly.")
                .build();

        String answer = client.chat("What is AiClient Builder?");

        System.out.println("answer:");
        System.out.println(answer);
    }

    private static void testAiClientStreaming(ChatModel model) {
        System.out.println();
        System.out.println("[16] AiClient Streaming test");

        AiClient client = VertexFlowAi.clientBuilder()
                .chatModel(model)
                .system("You are the streaming assistant of VertexFlow AI Framework. Answer briefly.")
                .build();

        client.stream("Introduce streaming chat in one short paragraph.", response -> {
            System.out.print(response.content());

            if (response.finished()) {
                System.out.println();
                System.out.println("[client stream finished]");
            }
        });
    }

    private static void testChatOptions(ChatModel model) {
        System.out.println();
        System.out.println("[17] ChatOptions test");

        ChatRequest request = new ChatRequest()
                .setOptions(ChatOptions.builder()
                        .temperature(0.2)
                        .maxTokens(300)
                        .topP(0.9)
                        .build())
                .addMessage(ChatMessage.system("You are a concise Java AI framework assistant."))
                .addMessage(ChatMessage.user("Explain ChatOptions in VertexFlow AI Framework in one sentence."));

        String answer = model.call(request).content();

        System.out.println("answer:");
        System.out.println(answer);
    }

    private static void testChatResponse(ChatModel model) {
        System.out.println();
        System.out.println("[18] ChatResponse test");

        ChatRequest request = new ChatRequest()
                .addMessage(ChatMessage.system("You are a concise Java AI framework assistant."))
                .addMessage(ChatMessage.user("Explain ChatResponse in VertexFlow AI Framework in one sentence."));

        ChatResponse response = model.call(request);

        System.out.println("content:");
        System.out.println(response.content());

        System.out.println("model: " + response.model());
        System.out.println("finishReason: " + response.finishReason());

        if (response.usage() != null) {
            System.out.println("inputTokens: " + response.usage().inputTokens());
            System.out.println("outputTokens: " + response.usage().outputTokens());
            System.out.println("totalTokens: " + response.usage().totalTokens());
        }

        System.out.println("rawResponse exists: " + (response.rawResponse() != null));
    }

    private static void testAiException() {
        System.out.println();
        System.out.println("[19] AiException test");

        try {
            TextFileDocumentLoader.loadFile("not-exist-file.txt");
        } catch (AiException e) {
            System.out.println("errorCode: " + e.getCode());
            System.out.println("message: " + e.getMessage());
        }
    }

    private static void testAiClientMemory(ChatModel model) {
        System.out.println();
        System.out.println("[20] AiClient Memory test");

        WindowChatMemory memory = new WindowChatMemory(10);

        AiClient client = AiClient.builder()
                .chatModel(model)
                .memory(memory)
                .conversationId("user-001")
                .system("You are a memory assistant. Remember user information in the conversation.")
                .build();

        String answer1 = client.chat("My name is Niu Guoqing. I am building VertexFlow AI Framework.");
        System.out.println("answer1:");
        System.out.println(answer1);

        String answer2 = client.chat("What is my name and what am I building?");
        System.out.println("answer2:");
        System.out.println(answer2);

        System.out.println("memory size: " + memory.get("user-001").size());
    }

    private static void testMemoryOptions() {
        System.out.println();
        System.out.println("[21] MemoryOptions test");

        WindowChatMemory memory = WindowChatMemory.builder()
                .maxMessages(3)
                .keepSystemMessage(false)
                .build();

        memory.add("memory-options-user", ChatMessage.user("message 1"));
        memory.add("memory-options-user", ChatMessage.assistant("message 2"));
        memory.add("memory-options-user", ChatMessage.user("message 3"));
        memory.add("memory-options-user", ChatMessage.assistant("message 4"));

        System.out.println("memory size: " + memory.get("memory-options-user").size());

        for (ChatMessage message : memory.get("memory-options-user")) {
            System.out.println(message);
        }

        WindowChatMemory memoryByOptions = new WindowChatMemory(
                MemoryOptions.builder()
                        .maxMessages(2)
                        .keepSystemMessage(false)
                        .build()
        );

        memoryByOptions.add("memory-options-user-2", ChatMessage.user("hello"));
        memoryByOptions.add("memory-options-user-2", ChatMessage.assistant("hi"));
        memoryByOptions.add("memory-options-user-2", ChatMessage.user("what is my first message?"));

        System.out.println("memoryByOptions size: " + memoryByOptions.get("memory-options-user-2").size());
    }

    private static void testToolCalling() {
        System.out.println();
        System.out.println("[22] Tool Calling test");

        ToolRegistry registry = new ToolRegistry();
        registry.register(new DemoWeatherTool());

        System.out.println("registered tools:");
        for (ToolDefinition definition : registry.list()) {
            System.out.println("- name: " + definition.name());
            System.out.println("  description: " + definition.description());
            System.out.println("  parameters: " + definition.parameters());
        }

        ToolResult weatherResult = registry.execute("getWeather", Map.of(
                "city", "Beijing"
        ));

        System.out.println("weather result:");
        System.out.println(weatherResult.content());

        ToolResult sumResult = registry.execute("calculateSum", Map.of(
                "a", 10,
                "b", 20
        ));

        System.out.println("sum result:");
        System.out.println(sumResult.content());
    }

    private static void testToolSchema() {
        System.out.println();
        System.out.println("[23] Tool Schema test");

        ToolRegistry registry = new ToolRegistry();
        registry.register(new DemoWeatherTool());

        System.out.println("basic schemas:");
        for (Map<String, Object> schema : registry.schemas()) {
            System.out.println(schema);
        }

        System.out.println();
        System.out.println("openai tool schemas:");
        for (Map<String, Object> schema : registry.openAiToolSchemas()) {
            System.out.println(schema);
        }

        ToolDefinition definition = registry.get("getWeather");
        System.out.println();
        System.out.println("single tool schema:");
        System.out.println(ToolSchemaGenerator.toSchema(definition));
    }

    private static void testToolCallExecutor() {
        System.out.println();
        System.out.println("[24] ToolCallExecutor test");

        ToolRegistry registry = new ToolRegistry();
        registry.register(new DemoWeatherTool());

        ToolCall weatherCall = new ToolCall("getWeather", Map.of(
                "city", "Shanghai"
        ));

        ToolCall sumCall = new ToolCall("calculateSum", Map.of(
                "a", 100,
                "b", 200
        ));

        ToolCallExecutor executor = ToolCallExecutor.create(registry);

        ToolCallResult weatherResult = executor.execute(weatherCall);
        System.out.println("weather tool call result:");
        System.out.println(weatherResult.result().content());

        ToolCallResult sumResult = executor.execute(sumCall);
        System.out.println("sum tool call result:");
        System.out.println(sumResult.result().content());

        System.out.println("execute all:");
        for (ToolCallResult result : executor.executeAll(List.of(weatherCall, sumCall))) {
            System.out.println("- " + result.toolCall().name() + ": " + result.result().content());
        }
    }

    private static void testOpenAiToolCallParser() {
        System.out.println();
        System.out.println("[25] OpenAI ToolCall Parser test");

        String rawResponse = """
            {
              "id": "chatcmpl-test",
              "object": "chat.completion",
              "created": 1234567890,
              "model": "deepseek-chat",
              "choices": [
                {
                  "index": 0,
                  "message": {
                    "role": "assistant",
                    "content": null,
                    "tool_calls": [
                      {
                        "id": "call_001",
                        "type": "function",
                        "function": {
                          "name": "getWeather",
                          "arguments": "{\\"city\\":\\"Beijing\\"}"
                        }
                      },
                      {
                        "id": "call_002",
                        "type": "function",
                        "function": {
                          "name": "calculateSum",
                          "arguments": "{\\"a\\":10,\\"b\\":20}"
                        }
                      }
                    ]
                  },
                  "finish_reason": "tool_calls"
                }
              ]
            }
            """;

        OpenAiToolCallParser parser = new OpenAiToolCallParser();

        List<ToolCall> toolCalls = parser.parse(rawResponse);

        for (ToolCall toolCall : toolCalls) {
            System.out.println("- name: " + toolCall.name());
            System.out.println("  arguments: " + toolCall.arguments());
        }

        ToolRegistry registry = new ToolRegistry();
        registry.register(new DemoWeatherTool());

        ToolCallExecutor executor = ToolCallExecutor.create(registry);

        System.out.println("execute parsed tool calls:");
        for (ToolCallResult result : executor.executeAll(toolCalls)) {
            System.out.println("- " + result.toolCall().name() + ": " + result.result().content());
        }
    }

    private static void testToolChatModel(ChatModel model) {
        System.out.println();
        System.out.println("[26] ToolChatModel test");

        if (!(model instanceof ToolChatModel toolChatModel)) {
            System.out.println("Current model does not support tool chat.");
            return;
        }

        ToolRegistry registry = new ToolRegistry();
        registry.register(new DemoWeatherTool());

        ChatRequest chatRequest = new ChatRequest()
                .addMessage(ChatMessage.system("You are a tool calling assistant. Use tools when needed."))
                .addMessage(ChatMessage.user("What is the weather in Beijing?"));

        ToolChatRequest request = ToolChatRequest.builder()
                .chatRequest(chatRequest)
                .tools(registry.openAiToolSchemas())
                .toolChoice("auto")
                .build();

        ChatResponse response = toolChatModel.callWithTools(request);

        System.out.println("finishReason: " + response.finishReason());
        System.out.println("content: " + response.content());

        OpenAiToolCallParser parser = new OpenAiToolCallParser();
        List<ToolCall> toolCalls = parser.parse(response.rawResponse());

        System.out.println("tool calls:");
        for (ToolCall toolCall : toolCalls) {
            System.out.println("- " + toolCall.name() + ": " + toolCall.arguments());
        }

        ToolCallExecutor executor = ToolCallExecutor.create(registry);

        System.out.println("tool results:");
        for (ToolCallResult result : executor.executeAll(toolCalls)) {
            System.out.println("- " + result.toolCall().name() + ": " + result.result().content());
        }
    }

    private static void testSimpleToolAgent(ChatModel model) {
        System.out.println();
        System.out.println("[27] SimpleToolAgent test");

        ToolRegistry registry = new ToolRegistry();
        registry.register(new DemoWeatherTool());

        SimpleToolAgent agent = SimpleToolAgent.builder()
                .chatModel(model)
                .toolRegistry(registry)
                .toolCallParser(new OpenAiToolCallParser())
                .maxSteps(10)
                .build();

        String answer = agent.chat("Use the getWeather tool to get the weather in Beijing, then answer me.");

        System.out.println("agent answer:");
        System.out.println(answer);
    }

    private static void testSimpleToolAgentTrace(ChatModel model) {
        System.out.println();
        System.out.println("[28] SimpleToolAgent Trace test");

        ToolRegistry registry = new ToolRegistry();
        registry.register(new DemoWeatherTool());

        SimpleToolAgent agent = SimpleToolAgent.builder()
                .chatModel(model)
                .toolRegistry(registry)
                .toolCallParser(new OpenAiToolCallParser())
                .maxSteps(10)
                .build();

        AgentResponse response = agent.run("You must call getWeather with city=Beijing, then give me the final answer.");

        System.out.println("agent answer:");
        System.out.println(response.answer());

        System.out.println();
        System.out.println("agent steps:");
        for (AgentStep step : response.steps()) {
            System.out.println("- type: " + step.type());
            System.out.println("  name: " + step.name());
            System.out.println("  content: " + step.content());
        }
    }

    private static void testSimpleToolAgentOptions(ChatModel model) {
        System.out.println();
        System.out.println("[29] SimpleToolAgent Options test");

        ToolRegistry registry = new ToolRegistry();
        registry.register(new DemoWeatherTool());

        SimpleToolAgent agent = SimpleToolAgent.builder()
                .chatModel(model)
                .toolRegistry(registry)
                .toolCallParser(new OpenAiToolCallParser())
                .options(AgentOptions.builder()
                        .maxSteps(10)
                        .returnSteps(true)
                        .build())
                .build();

        AgentResponse response = agent.run(
                "Do not answer directly. You must call the getWeather tool with city=Beijing first, then answer."
        );

        System.out.println("answer:");
        System.out.println(response.answer());

        System.out.println("steps size: " + response.steps().size());
    }

    private static void testAgentException(ChatModel model) {
        System.out.println();
        System.out.println("[30] AgentException test");

        ToolRegistry registry = new ToolRegistry();
        registry.register(new DemoWeatherTool());

        SimpleToolAgent agent = SimpleToolAgent.builder()
                .chatModel(model)
                .toolRegistry(registry)
                .toolCallParser(new OpenAiToolCallParser())
                .maxSteps(1)
                .build();

        try {
            agent.run("You must call getWeather with city=Beijing, then answer.");
        } catch (AgentException e) {
            System.out.println("agent errorCode: " + e.getCode());
            System.out.println("agent message: " + e.getMessage());
        }
    }

    private static void testToolFailurePropagation() {
        System.out.println();
        System.out.println("[31] Tool failure propagation test");

        ToolRegistry registry = new ToolRegistry();
        registry.register(new DemoWeatherTool());

        ToolCall toolCall = new ToolCall("failTool", Map.of(
                "reason", "testing failure propagation"
        ));

        ToolCallExecutor executor = ToolCallExecutor.create(registry);
        ToolCallResult result = executor.execute(toolCall);

        System.out.println("tool success: " + result.result().success());
        System.out.println("tool errorCode: " + result.result().errorCode());
        System.out.println("tool errorMessage: " + result.result().errorMessage());
    }

    private static void testQdrantVectorStore(ChatModel model) {
        System.out.println();
        System.out.println("[32] QdrantVectorStore test");

        VectorStore vectorStore = QdrantVectorStore.builder()
                .url("http://localhost:6333")
                .collectionName("vertexflow_docs")
                .vectorSize(256)
                .embeddingModel(new SimpleTextEmbedding(256))
                .build();

        RagEngine rag = new RagEngine(
                model,
                vectorStore,
                new MarkdownDocumentSplitter(300, 50),
                RagOptions.defaults()
                        .setTopK(3)
                        .setReturnSources(true)
        );

        rag.addDocument(new Document("qdrant-doc-1", """
            VertexFlow AI Framework 是一个面向 Java 开发者的轻量级 AI 应用开发框架。
            它支持 ChatModel、StreamingChatModel、AiClient、Memory、RAG、Tool Calling、Agent 和 Spring Boot Starter。
            QdrantVectorStore 可以将文档向量存储到 Qdrant 中，用于真实知识库检索。
            """));

        RagAnswer answer = rag.askWithSources("VertexFlow AI Framework 支持哪些能力？");

        System.out.println("answer:");
        System.out.println(answer.content());

        System.out.println();
        System.out.println("sources:");
        for (RagSource source : answer.sources()) {
            System.out.println("- documentId: " + source.documentId());
            System.out.println("  chunkId: " + source.chunkId());
            System.out.println("  score: " + source.score());
            System.out.println("  content: " + source.content());
        }
    }
    private static void testPdfDocumentLoader(ChatModel model) {
        System.out.println();
        System.out.println("[33] PdfDocumentLoader test");

        String pdfPath = "vertexflow-ai-examples/src/main/resources/doc/vertexflow-intro.pdf";

        Document document = PdfDocumentLoader.loadFile(pdfPath);

        System.out.println("documentId: " + document.id());
        System.out.println("content length: " + document.content().length());

        RagEngine rag = new RagEngine(
                model,
                new InMemoryVectorStore(new SimpleTextEmbedding(256)),
                new MarkdownDocumentSplitter(300, 50),
                RagOptions.defaults()
                        .setTopK(3)
                        .setReturnSources(true)
        );

        rag.addDocument(document);

        RagAnswer answer = rag.askWithSources("VertexFlow AI Framework 支持哪些能力？");

        System.out.println("answer:");
        System.out.println(answer.content());

        System.out.println("sources:");
        for (RagSource source : answer.sources()) {
            System.out.println("- documentId: " + source.documentId());
            System.out.println("  chunkId: " + source.chunkId());
            System.out.println("  score: " + source.score());
            System.out.println("  content: " + source.content());
        }
    }

    private static void testUrlDocumentLoader(ChatModel model) {
        System.out.println();
        System.out.println("[34] UrlDocumentLoader test");

        Document document = UrlDocumentLoader.load("https://example.com");

        System.out.println("documentId: " + document.id());
        System.out.println("content length: " + document.content().length());

        RagEngine rag = new RagEngine(
                model,
                new InMemoryVectorStore(new SimpleTextEmbedding(256)),
                new MarkdownDocumentSplitter(300, 50),
                RagOptions.defaults()
                        .setTopK(3)
                        .setReturnSources(true)
        );

        rag.addDocument(document);

        RagAnswer answer = rag.askWithSources("这个网页主要讲了什么？");

        System.out.println("answer:");
        System.out.println(answer.content());

        System.out.println("sources:");
        for (RagSource source : answer.sources()) {
            System.out.println("- documentId: " + source.documentId());
            System.out.println("  chunkId: " + source.chunkId());
            System.out.println("  score: " + source.score());
            System.out.println("  content: " + source.content());
        }
    }

    private static void testAddDocumentResult(ChatModel model) {
        System.out.println();
        System.out.println("[35] AddDocumentResult test");

        RagEngine rag = new RagEngine(
                model,
                new InMemoryVectorStore(new SimpleTextEmbedding(256)),
                new MarkdownDocumentSplitter(300, 50),
                RagOptions.defaults()
                        .setTopK(3)
                        .setReturnSources(true)
        );

        Document document = new Document("add-result-doc", """
            VertexFlow AI Framework supports ChatModel, Memory, RAG, Tool Calling and Spring Boot Starter.
            AddDocumentResult can show total chunks, added chunks and skipped chunks.
            """);

        AddDocumentResult first = rag.addDocumentWithResult(document);

        System.out.println("first add:");
        System.out.println("documentId: " + first.documentId());
        System.out.println("totalChunks: " + first.totalChunks());
        System.out.println("addedChunks: " + first.addedChunks());
        System.out.println("skippedChunks: " + first.skippedChunks());

        AddDocumentResult second = rag.addDocumentWithResult(document);

        System.out.println("second add:");
        System.out.println("documentId: " + second.documentId());
        System.out.println("totalChunks: " + second.totalChunks());
        System.out.println("addedChunks: " + second.addedChunks());
        System.out.println("skippedChunks: " + second.skippedChunks());
    }

    private static void testDeleteDocument(ChatModel model) {
        System.out.println();
        System.out.println("[36] DeleteDocument test");

        RagEngine rag = new RagEngine(
                model,
                new InMemoryVectorStore(new SimpleTextEmbedding(256)),
                new MarkdownDocumentSplitter(300, 50),
                RagOptions.defaults()
                        .setTopK(3)
                        .setReturnSources(true)
        );

        Document document = new Document("delete-doc", """
            VertexFlow AI Framework supports deleteDocument.
            This document should be removed from vector store.
            """);

        AddDocumentResult addResult = rag.addDocumentWithResult(document);

        System.out.println("addedChunks: " + addResult.addedChunks());

        RagAnswer beforeDelete = rag.askWithSources("What does delete-doc support?");
        System.out.println("before delete sources size: " + beforeDelete.sources().size());

        int deleted = rag.deleteDocument("delete-doc");
        System.out.println("deleted chunks: " + deleted);

        RagAnswer afterDelete = rag.askWithSources("What does delete-doc support?");
        System.out.println("after delete sources size: " + afterDelete.sources().size());
    }

    private static void testReActAgent(ChatModel model) {
        System.out.println();
        System.out.println("[37] ReActAgent test");

        ToolRegistry registry = new ToolRegistry();
        registry.register(new DemoWeatherTool());

        ReActAgent agent = ReActAgent.builder()
                .chatModel(model)
                .toolRegistry(registry)
                .maxSteps(5)
                .build();

        AgentResponse response = agent.run(
                "你必须先调用 getWeather 工具查询北京天气，然后用中文告诉我结果。"
        );

        System.out.println("answer:");
        System.out.println(response.answer());

        System.out.println();
        System.out.println("steps:");
        for (AgentStep step : response.steps()) {
            System.out.println("- type: " + step.type());
            System.out.println("  name: " + step.name());
            System.out.println("  content: " + step.content());
        }
    }

    private static void testReActAgentJsonInput(ChatModel model) {
        System.out.println();
        System.out.println("[38] ReActAgent JSON Action Input test");

        ToolRegistry registry = new ToolRegistry();
        registry.register(new DemoWeatherTool());

        ReActAgent agent = ReActAgent.builder()
                .chatModel(model)
                .toolRegistry(registry)
                .options(ReActAgentOptions.builder()
                        .maxSteps(5)
                        .returnSteps(true)
                        .allowJsonActionInput(true)
                        .build())
                .build();

        AgentResponse response = agent.run("""
            请严格按照 ReAct 格式执行。
            你必须先调用 getWeather 工具。
            Action Input 必须使用 JSON：{"city":"Beijing"}
            然后根据工具结果给出中文 Final Answer。
            """);

        System.out.println("answer:");
        System.out.println(response.answer());

        System.out.println();
        System.out.println("steps:");
        for (AgentStep step : response.steps()) {
            System.out.println("- type: " + step.type());
            System.out.println("  name: " + step.name());
            System.out.println("  content: " + step.content());
        }
    }

    private static void testReActAgentFormatRecovery(ChatModel model) {
        System.out.println();
        System.out.println("[39] ReActAgent Format Recovery test");

        ToolRegistry registry = new ToolRegistry();
        registry.register(new DemoWeatherTool());

        ReActAgent agent = ReActAgent.builder()
                .chatModel(model)
                .toolRegistry(registry)
                .options(ReActAgentOptions.builder()
                        .maxSteps(6)
                        .returnSteps(true)
                        .allowJsonActionInput(true)
                        .retryOnFormatError(true)
                        .build())
                .build();

        AgentResponse response = agent.run("""
            你需要查询北京天气。
            如果第一次格式不规范，请根据系统提示修正格式。
            最终用中文回答。
            """);

        System.out.println("answer:");
        System.out.println(response.answer());

        System.out.println();
        System.out.println("steps:");
        for (AgentStep step : response.steps()) {
            System.out.println("- type: " + step.type());
            System.out.println("  name: " + step.name());
            System.out.println("  content: " + step.content());
        }
    }

    private static void testReActAgentFailureReason(ChatModel model) {
        System.out.println();
        System.out.println("[40] ReActAgent Failure Reason test");

        ToolRegistry registry = new ToolRegistry();
        registry.register(new DemoWeatherTool());

        ReActAgent agent = ReActAgent.builder()
                .chatModel(model)
                .toolRegistry(registry)
                .options(ReActAgentOptions.builder()
                        .maxSteps(1)
                        .returnSteps(true)
                        .allowJsonActionInput(true)
                        .retryOnFormatError(true)
                        .build())
                .build();

        AgentResponse response = agent.run("""
            严格按照 ReAct 格式执行。
            你必须调用 getWeather 工具查询北京天气，然后回答。
            """);

        System.out.println("success: " + response.success());
        System.out.println("failureReason: " + response.failureReason());
        System.out.println("answer:");
        System.out.println(response.answer());

        System.out.println();
        System.out.println("steps:");
        for (AgentStep step : response.steps()) {
            System.out.println("- type: " + step.type());
            System.out.println("  name: " + step.name());
            System.out.println("  content: " + step.content());
        }
    }

    private static void testReActAgentToolNotFound(ChatModel model) {
        System.out.println();
        System.out.println("[41] ReActAgent Tool Not Found test");

        ToolRegistry registry = new ToolRegistry();
        registry.register(new DemoWeatherTool());

        ReActAgent agent = ReActAgent.builder()
                .chatModel(model)
                .toolRegistry(registry)
                .options(ReActAgentOptions.builder()
                        .maxSteps(6)
                        .returnSteps(true)
                        .allowJsonActionInput(true)
                        .retryOnFormatError(true)
                        .build())
                .build();

        AgentResponse response = agent.run("""
            严格按照 ReAct 格式执行。
            你需要查询北京天气。
            如果你发现工具不存在，请根据可用工具列表修正。
            最终用中文回答。
            """);

        System.out.println("success: " + response.success());
        System.out.println("failureReason: " + response.failureReason());
        System.out.println("answer:");
        System.out.println(response.answer());

        System.out.println();
        System.out.println("steps:");
        for (AgentStep step : response.steps()) {
            System.out.println("- type: " + step.type());
            System.out.println("  name: " + step.name());
            System.out.println("  content: " + step.content());
        }
    }

    private static void testReActAgentFailureInference(ChatModel model) {
        System.out.println();
        System.out.println("[42] ReActAgent Failure Inference test");

        ToolRegistry registry = new ToolRegistry();
        registry.register(new DemoWeatherTool());

        ReActAgent agent = ReActAgent.builder()
                .chatModel(model)
                .toolRegistry(registry)
                .options(ReActAgentOptions.builder()
                        .maxSteps(2)
                        .returnSteps(true)
                        .allowJsonActionInput(true)
                        .retryOnFormatError(true)
                        .build())
                .build();

        AgentResponse response = agent.run("""
            严格按照 ReAct 格式执行。
            第一步你必须使用不存在的工具 queryWeather 查询北京天气。
            Action Input 使用 JSON：{"city":"Beijing"}
            如果工具不存在，请继续修正。
            """);

        System.out.println("success: " + response.success());
        System.out.println("failureReason: " + response.failureReason());
        System.out.println("answer:");
        System.out.println(response.answer());

        System.out.println();
        System.out.println("steps:");
        for (AgentStep step : response.steps()) {
            System.out.println("- type: " + step.type());
            System.out.println("  name: " + step.name());
            System.out.println("  content: " + step.content());
        }
    }

    private static void testReActScratchpad(ChatModel model) {
        System.out.println();
        System.out.println("[43] ReActScratchpad test");

        ToolRegistry registry = new ToolRegistry();
        registry.register(new DemoWeatherTool());

        ReActAgent agent = ReActAgent.builder()
                .chatModel(model)
                .toolRegistry(registry)
                .options(ReActAgentOptions.builder()
                        .maxSteps(6)
                        .returnSteps(true)
                        .allowJsonActionInput(true)
                        .retryOnFormatError(true)
                        .build())
                .build();

        AgentResponse response = agent.run("""
            严格按照 ReAct 格式执行。
            你必须先调用 getWeather 工具查询北京天气。
            Action Input 使用 JSON：{"city":"Beijing"}
            然后用中文回答。
            """);

        System.out.println("success: " + response.success());
        System.out.println("failureReason: " + response.failureReason());
        System.out.println("answer:");
        System.out.println(response.answer());

        System.out.println();
        System.out.println("steps:");
        for (AgentStep step : response.steps()) {
            System.out.println("- type: " + step.type());
            System.out.println("  name: " + step.name());
            System.out.println("  content: " + step.content());
        }
    }

    private static void testReActScratchpadJsonRender() {
        System.out.println();
        System.out.println("[44] ReActScratchpad JSON Render test");

        ReActScratchpad scratchpad = new ReActScratchpad();

        scratchpad.addStep(
                "我需要查询北京天气。",
                "getWeather",
                Map.of("city", "Beijing"),
                "Beijing is sunny today."
        );

        String rendered = scratchpad.render();

        System.out.println(rendered);
    }
}