package com.vertexflow.ai.core.tool;

import com.vertexflow.ai.core.chat.ChatMessage;
import com.vertexflow.ai.core.chat.ChatModel;
import com.vertexflow.ai.core.chat.ChatRequest;
import com.vertexflow.ai.core.chat.ChatResponse;
import com.vertexflow.ai.core.exception.AiErrorCode;
import com.vertexflow.ai.core.exception.AiException;

import java.util.List;
import java.util.stream.Collectors;

public class SimpleToolAgent {

    private final ChatModel chatModel;
    private final ToolRegistry toolRegistry;
    private final ToolCallParser toolCallParser;

    private SimpleToolAgent(Builder builder) {
        this.chatModel = builder.chatModel;
        this.toolRegistry = builder.toolRegistry;
        this.toolCallParser = builder.toolCallParser;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String chat(String userMessage) {
        ChatResponse response = callWithTools(userMessage);

        List<ToolCall> toolCalls = toolCallParser.parse(response.rawResponse());

        if (toolCalls.isEmpty()) {
            return response.content();
        }

        ToolCallExecutor executor = ToolCallExecutor.create(toolRegistry);
        List<ToolCallResult> results = executor.executeAll(toolCalls);

        String toolResultText = results.stream()
                .map(result -> "Tool: " + result.toolCall().name()
                        + "\nArguments: " + result.toolCall().arguments()
                        + "\nResult: " + result.result().content())
                .collect(Collectors.joining("\n\n"));

        ChatRequest finalRequest = new ChatRequest()
                .addMessage(ChatMessage.system("""
                        You are a helpful AI assistant.
                        The user asked a question and tools have been executed.
                        Please generate the final answer based on the tool results.
                        """))
                .addMessage(ChatMessage.user("""
                        User question:
                        %s

                        Tool results:
                        %s
                        """.formatted(userMessage, toolResultText)));

        return chatModel.call(finalRequest).content();
    }

    private ChatResponse callWithTools(String userMessage) {
        if (!(chatModel instanceof ToolChatModel toolChatModel)) {
            throw new AiException(AiErrorCode.UNSUPPORTED_OPERATION, "Current chatModel does not support tool chat");
        }

        ChatRequest chatRequest = new ChatRequest()
                .addMessage(ChatMessage.system("You are a tool calling assistant. Use tools when needed."))
                .addMessage(ChatMessage.user(userMessage));

        ToolChatRequest toolChatRequest = ToolChatRequest.builder()
                .chatRequest(chatRequest)
                .tools(toolRegistry.openAiToolSchemas())
                .toolChoice("auto")
                .build();

        return toolChatModel.callWithTools(toolChatRequest);
    }

    public static class Builder {

        private ChatModel chatModel;
        private ToolRegistry toolRegistry;
        private ToolCallParser toolCallParser;

        public Builder chatModel(ChatModel chatModel) {
            this.chatModel = chatModel;
            return this;
        }

        public Builder toolRegistry(ToolRegistry toolRegistry) {
            this.toolRegistry = toolRegistry;
            return this;
        }

        public Builder toolCallParser(ToolCallParser toolCallParser) {
            this.toolCallParser = toolCallParser;
            return this;
        }

        public SimpleToolAgent build() {
            if (chatModel == null) {
                throw new IllegalArgumentException("chatModel is required");
            }

            if (toolRegistry == null) {
                throw new IllegalArgumentException("toolRegistry is required");
            }

            if (toolCallParser == null) {
                throw new IllegalArgumentException("toolCallParser is required");
            }

            return new SimpleToolAgent(this);
        }
    }
}