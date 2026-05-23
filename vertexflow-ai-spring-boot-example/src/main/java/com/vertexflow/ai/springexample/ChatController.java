package com.vertexflow.ai.springexample;

import com.vertexflow.ai.core.chat.AiClient;
import com.vertexflow.ai.rag.Document;
import com.vertexflow.ai.rag.RagAnswer;
import com.vertexflow.ai.rag.RagEngine;
import com.vertexflow.ai.rag.RagSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final AiClient aiClient;
    private final RagEngine ragEngine;

    public ChatController(AiClient aiClient, RagEngine ragEngine) {
        this.aiClient = aiClient;
        this.ragEngine = ragEngine;

        this.ragEngine.addDocument(new Document("spring-demo-doc", """
                VertexFlow AI Framework is a lightweight AI application development framework for Java developers.
                It supports ChatModel, StreamingChatModel, AiClient, Memory, RAG, Tool Calling and Spring Boot Starter.
                The Spring Boot Starter can automatically configure AiClient and RagEngine.
                """));
    }

    @GetMapping("/chat")
    public String chat(@RequestParam("message") String message) {
        return aiClient.chat(message);
    }

    @GetMapping("/memory-chat")
    public String memoryChat(@RequestParam("message") String message) {
        return aiClient.chat(message);
    }

    @GetMapping("/rag")
    public String rag(@RequestParam("question") String question) {
        RagAnswer answer = ragEngine.askWithSources(question);

        StringBuilder builder = new StringBuilder();
        builder.append("answer:\n")
                .append(answer.content())
                .append("\n\nsources:\n");

        for (RagSource source : answer.sources()) {
            builder.append("- documentId: ")
                    .append(source.documentId())
                    .append("\n  score: ")
                    .append(source.score())
                    .append("\n  content: ")
                    .append(source.content())
                    .append("\n");
        }

        return builder.toString();
    }
}