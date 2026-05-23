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

        testChat(model);
        testStream(model);
        testRag(model);
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
        System.out.println("[5] RagEngine test");

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
}