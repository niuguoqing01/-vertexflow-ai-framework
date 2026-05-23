package com.vertexflow.ai.examples;

import com.vertexflow.ai.core.chat.AiClient;
import com.vertexflow.ai.core.chat.ChatMessage;
import com.vertexflow.ai.core.chat.ChatModel;
import com.vertexflow.ai.core.prompt.PromptTemplate;
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
import com.vertexflow.ai.core.tool.ToolDefinition;
import com.vertexflow.ai.core.tool.ToolRegistry;
import com.vertexflow.ai.core.tool.ToolResult;
import com.vertexflow.ai.core.tool.ToolSchemaGenerator;
import com.vertexflow.ai.core.tool.ToolCall;
import com.vertexflow.ai.core.tool.ToolCallExecutor;
import com.vertexflow.ai.core.tool.ToolCallResult;

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
}