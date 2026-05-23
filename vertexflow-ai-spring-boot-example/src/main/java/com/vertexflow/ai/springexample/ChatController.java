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
import com.vertexflow.ai.spring.boot.starter.RagDocumentAutoLoader;
import com.vertexflow.ai.core.tool.ReActAgent;
import com.vertexflow.ai.core.tool.AgentTraceJsonExporter;
import com.vertexflow.ai.core.chat.ChatMessage;
import com.vertexflow.ai.core.memory.ChatMemory;
import com.vertexflow.ai.core.chat.ChatMessage;
import com.vertexflow.ai.core.memory.ChatMemory;
import java.util.List;

import java.util.List;

@RestController
public class ChatController {

    private final AiClient aiClient;
    private final ObjectProvider<RagEngine> ragEngineProvider;
    private final ObjectProvider<SimpleToolAgent> simpleToolAgentProvider;
    private final ObjectProvider<RagDocumentAutoLoader> ragDocumentAutoLoaderProvider;
    private final ObjectProvider<ReActAgent> reActAgentProvider;
    private final ObjectProvider<ChatMemory> chatMemoryProvider;

    public ChatController(
            AiClient aiClient,
            ObjectProvider<RagEngine> ragEngineProvider,
            ObjectProvider<SimpleToolAgent> simpleToolAgentProvider,
            ObjectProvider<RagDocumentAutoLoader> ragDocumentAutoLoaderProvider,
            ObjectProvider<ReActAgent> reActAgentProvider,
            ObjectProvider<ChatMemory> chatMemoryProvider
    ) {
        this.aiClient = aiClient;
        this.ragEngineProvider = ragEngineProvider;
        this.simpleToolAgentProvider = simpleToolAgentProvider;
        this.ragDocumentAutoLoaderProvider = ragDocumentAutoLoaderProvider;
        this.reActAgentProvider = reActAgentProvider;
        this.chatMemoryProvider = chatMemoryProvider;
    }

    @GetMapping("/rag/reload")
    public String reloadRag() {
        RagDocumentAutoLoader loader = ragDocumentAutoLoaderProvider.getIfAvailable();

        if (loader == null) {
            return "RagDocumentAutoLoader is not enabled. Please set vertexflow.ai.rag.enabled=true";
        }

        RagDocumentAutoLoader.RagLoadResult result = loader.reload();

        return """
            RAG reload finished.
            documentCount: %s
            totalChunks: %s
            addedChunks: %s
            skippedChunks: %s
            """.formatted(
                result.documentCount(),
                result.totalChunks(),
                result.addedChunks(),
                result.skippedChunks()
        );
    }

    @GetMapping("/rag/status")
    public String ragStatus() {
        RagDocumentAutoLoader loader = ragDocumentAutoLoaderProvider.getIfAvailable();

        if (loader == null) {
            return "RagDocumentAutoLoader is not enabled.";
        }

        RagDocumentAutoLoader.RagLoadResult result = loader.lastResult();

        return """
            RAG status:
            documentCount: %s
            totalChunks: %s
            addedChunks: %s
            skippedChunks: %s
            """.formatted(
                result.documentCount(),
                result.totalChunks(),
                result.addedChunks(),
                result.skippedChunks()
        );
    }

    @GetMapping("/rag/delete")
    public String deleteRagDocument(@RequestParam("documentId") String documentId) {
        RagEngine ragEngine = ragEngineProvider.getIfAvailable();

        if (ragEngine == null) {
            return "RagEngine is not enabled. Please set vertexflow.ai.rag.enabled=true";
        }

        int deleted = ragEngine.deleteDocument(documentId);

        return """
            RAG document delete finished.
            documentId: %s
            deletedChunks: %s
            """.formatted(documentId, deleted);
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
        builder.append("success:\n")
                .append(response.success())
                .append("\n\nfailureReason:\n")
                .append(response.failureReason())
                .append("\n\nanswer:\n")
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

    @GetMapping("/agent/react")
    public String reactAgent(@RequestParam("message") String message) {
        ReActAgent agent = reActAgentProvider.getIfAvailable();

        if (agent == null) {
            return "ReActAgent is not enabled. Please set vertexflow.ai.tool.enabled=true and vertexflow.ai.tool.react-enabled=true";
        }

        AgentResponse response = agent.run(message);

        StringBuilder builder = new StringBuilder();
        builder.append("success:\n")
                .append(response.success())
                .append("\n\nfailureReason:\n")
                .append(response.failureReason())
                .append("\n\nanswer:\n")
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

    @GetMapping(value = "/agent/react/trace", produces = "application/json;charset=UTF-8")
    public String reactAgentTrace(@RequestParam("message") String message) {
        ReActAgent agent = reActAgentProvider.getIfAvailable();

        if (agent == null) {
            return """
                {
                  "success": false,
                  "failureReason": "REACT_AGENT_NOT_ENABLED",
                  "answer": "ReActAgent is not enabled. Please set vertexflow.ai.tool.enabled=true and vertexflow.ai.tool.react-enabled=true",
                  "steps": []
                }
                """;
        }

        AgentResponse response = agent.run(message);

        return AgentTraceJsonExporter.create().toJson(response);
    }

    @GetMapping("/memory/status")
    public String memoryStatus() {
        ChatMemory memory = chatMemoryProvider.getIfAvailable();

        if (memory == null) {
            return "ChatMemory is not enabled. Please set vertexflow.ai.memory.enabled=true";
        }

        return """
            Memory status:
            enabled: true
            implementation: %s
            """.formatted(memory.getClass().getSimpleName());
    }

    @GetMapping("/memory/messages")
    public String memoryMessages(@RequestParam("conversationId") String conversationId) {
        ChatMemory memory = chatMemoryProvider.getIfAvailable();

        if (memory == null) {
            return "ChatMemory is not enabled. Please set vertexflow.ai.memory.enabled=true";
        }

        List<ChatMessage> messages = memory.get(conversationId);

        StringBuilder builder = new StringBuilder();

        builder.append("conversationId: ")
                .append(conversationId)
                .append("\n");

        builder.append("messageCount: ")
                .append(messages.size())
                .append("\n\n");

        for (ChatMessage message : messages) {
            builder.append("- role: ")
                    .append(message.role())
                    .append("\n  content: ")
                    .append(message.content())
                    .append("\n");
        }

        return builder.toString();
    }

    @GetMapping("/memory/clear")
    public String clearMemory(@RequestParam("conversationId") String conversationId) {
        ChatMemory memory = chatMemoryProvider.getIfAvailable();

        if (memory == null) {
            return "ChatMemory is not enabled. Please set vertexflow.ai.memory.enabled=true";
        }

        memory.clear(conversationId);

        return """
            Memory cleared.
            conversationId: %s
            """.formatted(conversationId);
    }

    @GetMapping(value = "/memory/status/json", produces = "application/json;charset=UTF-8")
    public MemoryStatusResponse memoryStatusJson() {
        ChatMemory memory = chatMemoryProvider.getIfAvailable();

        if (memory == null) {
            return new MemoryStatusResponse(false, null);
        }

        return new MemoryStatusResponse(
                true,
                memory.getClass().getSimpleName()
        );
    }

    @GetMapping(value = "/memory/messages/json", produces = "application/json;charset=UTF-8")
    public MemoryMessagesResponse memoryMessagesJson(@RequestParam("conversationId") String conversationId) {
        ChatMemory memory = chatMemoryProvider.getIfAvailable();

        if (memory == null) {
            return new MemoryMessagesResponse(
                    conversationId,
                    0,
                    List.of()
            );
        }

        List<ChatMessage> messages = memory.get(conversationId);

        List<MemoryMessageResponse> result = messages.stream()
                .map(message -> new MemoryMessageResponse(
                        String.valueOf(message.role()),
                        message.content()
                ))
                .toList();

        return new MemoryMessagesResponse(
                conversationId,
                result.size(),
                result
        );
    }

    @GetMapping(value = "/memory/clear/json", produces = "application/json;charset=UTF-8")
    public MemoryClearResponse clearMemoryJson(@RequestParam("conversationId") String conversationId) {
        ChatMemory memory = chatMemoryProvider.getIfAvailable();

        if (memory == null) {
            return new MemoryClearResponse(
                    false,
                    conversationId,
                    "ChatMemory is not enabled. Please set vertexflow.ai.memory.enabled=true"
            );
        }

        memory.clear(conversationId);

        return new MemoryClearResponse(
                true,
                conversationId,
                "Memory cleared"
        );
    }
}