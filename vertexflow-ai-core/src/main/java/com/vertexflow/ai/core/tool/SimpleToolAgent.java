package com.vertexflow.ai.core.tool;

import com.vertexflow.ai.core.chat.ChatMessage;
import com.vertexflow.ai.core.chat.ChatModel;
import com.vertexflow.ai.core.chat.ChatRequest;
import com.vertexflow.ai.core.chat.ChatResponse;
import com.vertexflow.ai.core.exception.AiErrorCode;
import com.vertexflow.ai.core.exception.AiException;
import com.vertexflow.ai.core.exception.AgentException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleToolAgent {

    private final ChatModel chatModel;
    private final ToolRegistry toolRegistry;
    private final ToolCallParser toolCallParser;
    private final AgentOptions options;

    private SimpleToolAgent(Builder builder) {
        this.chatModel = builder.chatModel;
        this.toolRegistry = builder.toolRegistry;
        this.toolCallParser = builder.toolCallParser;
        this.options = builder.options == null ? AgentOptions.defaults() : builder.options;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String chat(String userMessage) {
        return run(userMessage).answer();
    }

    public AgentResponse run(String userMessage) {
        List<AgentStep> steps = new ArrayList<>();
        int currentStep = 0;

        steps.add(new AgentStep(
                AgentStepType.USER_INPUT,
                "user",
                userMessage,
                userMessage
        ));
        currentStep++;
        checkMaxSteps(currentStep);

        ChatResponse response = callWithTools(userMessage);

        steps.add(new AgentStep(
                AgentStepType.MODEL_RESPONSE,
                "tool_chat_model",
                response.content(),
                response
        ));

        currentStep++;
        checkMaxSteps(currentStep);

        List<ToolCall> toolCalls;
        try {
            toolCalls = toolCallParser.parse(response.rawResponse());
        } catch (Exception e) {
            throw AgentException.toolParseError("Failed to parse tool calls from model response", e);
        }

        if (toolCalls.isEmpty()) {
            String answer = response.content();

            steps.add(new AgentStep(
                    AgentStepType.FINAL_ANSWER,
                    "final_answer",
                    answer,
                    answer
            ));

            return new AgentResponse(answer, steps);
        }

        for (ToolCall toolCall : toolCalls) {
            currentStep++;
            checkMaxSteps(currentStep);
            steps.add(new AgentStep(
                    AgentStepType.TOOL_CALL,
                    toolCall.name(),
                    String.valueOf(toolCall.arguments()),
                    toolCall
            ));
        }

        ToolCallExecutor executor = ToolCallExecutor.create(toolRegistry);

        List<ToolCallResult> results;
        try {
            results = executor.executeAll(toolCalls);
        } catch (Exception e) {
            throw AgentException.toolCallError("Failed to execute tool calls", e);
        }

        for (ToolCallResult result : results) {
            currentStep++;
            checkMaxSteps(currentStep);

            ToolResult toolResult = result.result();

            String stepContent = toolResult.success()
                    ? toolResult.content()
                    : "Tool failed. errorCode=" + toolResult.errorCode()
                    + ", errorMessage=" + toolResult.errorMessage();

            steps.add(new AgentStep(
                    AgentStepType.TOOL_RESULT,
                    result.toolCall().name(),
                    stepContent,
                    result
            ));
        }

        String toolResultText = results.stream()
                .map(result -> {
                    ToolResult toolResult = result.result();

                    if (toolResult.success()) {
                        return "Tool: " + result.toolCall().name()
                                + "\nArguments: " + result.toolCall().arguments()
                                + "\nSuccess: true"
                                + "\nResult: " + toolResult.content();
                    }

                    return "Tool: " + result.toolCall().name()
                            + "\nArguments: " + result.toolCall().arguments()
                            + "\nSuccess: false"
                            + "\nErrorCode: " + toolResult.errorCode()
                            + "\nErrorMessage: " + toolResult.errorMessage();
                })
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
        currentStep++;
        checkMaxSteps(currentStep);
        String finalAnswer = chatModel.call(finalRequest).content();

        steps.add(new AgentStep(
                AgentStepType.FINAL_ANSWER,
                "final_answer",
                finalAnswer,
                finalAnswer
        ));

        return new AgentResponse(finalAnswer, steps);
    }
    private void checkMaxSteps(int currentStep) {
        if (currentStep > options.getMaxSteps()) {
            throw AgentException.maxStepsExceeded(options.getMaxSteps());
        }
    }
    private ChatResponse callWithTools(String userMessage) {
        if (!(chatModel instanceof ToolChatModel toolChatModel)) {
            throw AgentException.modelNotSupportTool();
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
        private AgentOptions options;

        public Builder options(AgentOptions options) {
            this.options = options;
            return this;
        }

        public Builder maxSteps(int maxSteps) {
            this.options = AgentOptions.builder()
                    .maxSteps(maxSteps)
                    .build();
            return this;
        }

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