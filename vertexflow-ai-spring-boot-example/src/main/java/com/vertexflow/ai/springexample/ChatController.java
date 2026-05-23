package com.vertexflow.ai.springexample;

import com.vertexflow.ai.core.chat.AiClient;
import com.vertexflow.ai.core.tool.AgentResponse;
import com.vertexflow.ai.core.tool.AgentStep;
import com.vertexflow.ai.core.tool.SimpleToolAgent;
import com.vertexflow.ai.rag.Document;
import com.vertexflow.ai.rag.RagAnswer;
import com.vertexflow.ai.rag.RagEngine;
import com.vertexflow.ai.rag.RagSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final AiClient aiClient;
    private final ObjectProvider<RagEngine> ragEngineProvider;
    private final ObjectProvider<SimpleToolAgent> simpleToolAgentProvider;

    public ChatController(
            AiClient aiClient,
            ObjectProvider<RagEngine> ragEngineProvider,
            ObjectProvider<SimpleToolAgent> simpleToolAgentProvider
    ) {
        this.aiClient = aiClient;
        this.ragEngineProvider = ragEngineProvider;
        this.simpleToolAgentProvider = simpleToolAgentProvider;

        RagEngine ragEngine = ragEngineProvider.getIfAvailable();

        if (ragEngine != null) {
            ragEngine.addDocument(new Document("spring-demo-doc", """
        VertexFlow AI Framework 是一个面向 Java 开发者的轻量级 AI 应用开发框架。
        它支持 ChatModel、StreamingChatModel、AiClient、Memory、RAG、Tool Calling 和 Spring Boot Starter。
        Spring Boot Starter 可以自动配置 AiClient、RagEngine 和 SimpleToolAgent。
        """));
        }
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
        RagEngine ragEngine = ragEngineProvider.getIfAvailable();

        if (ragEngine == null) {
            return "RagEngine is not enabled. Please set vertexflow.ai.rag.enabled=true";
        }

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

    @GetMapping("/agent")
    public String agent(@RequestParam("message") String message) {
        SimpleToolAgent agent = simpleToolAgentProvider.getIfAvailable();

        if (agent == null) {
            return "SimpleToolAgent is not enabled. Please set vertexflow.ai.tool.enabled=true";
        }

        AgentResponse response = agent.run(message);

        StringBuilder builder = new StringBuilder();
        builder.append("answer:\n")
                .append(response.answer())
                .append("\n\nsteps:\n");

        for (AgentStep step : response.steps()) {
            builder.append("- type: ")
                    .append(step.type())
                    .append("\n  name: ")
                    .append(step.name())
                    .append("\n  content: ")
                    .append(step.content())
                    .append("\n");
        }

        return builder.toString();
    }
}