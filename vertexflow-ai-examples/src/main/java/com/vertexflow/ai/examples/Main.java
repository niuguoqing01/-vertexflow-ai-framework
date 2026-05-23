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
                .build();
        EmbeddingModel embeddingModel = OpenAiCompatibleEmbeddingModel.builder()
                .apiKey(apiKey)
                .baseUrl("https://api.deepseek.com/v1")
                .model("text-embedding-v1")
                .build();
        testChat(model);
        testStream(model);
        testEmbedding(new SimpleTextEmbedding(256));
        testVectorStore();
        testRag(model);
        testRagWithSources(model);
        testDocumentLoader(model);
        testDirectoryDocumentLoader(model);
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

        DocumentSplitter splitter = new DocumentSplitter(100, 20);
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
}