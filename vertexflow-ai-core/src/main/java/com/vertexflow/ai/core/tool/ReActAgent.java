package com.vertexflow.ai.core.tool;

import com.vertexflow.ai.core.chat.ChatMessage;
import com.vertexflow.ai.core.chat.ChatModel;
import com.vertexflow.ai.core.chat.ChatRequest;
import com.vertexflow.ai.core.chat.ChatResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReActAgent {

    private final ChatModel chatModel;
    private final ToolRegistry toolRegistry;
    private final ReActAgentOptions options;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ReActAgent(Builder builder) {
        this.chatModel = builder.chatModel;
        this.toolRegistry = builder.toolRegistry;
        this.options = builder.options == null ? ReActAgentOptions.defaults() : builder.options;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String chat(String userMessage) {
        return run(userMessage).answer();
    }

    public AgentResponse run(String userMessage) {
        List<AgentStep> steps = new ArrayList<>();

        steps.add(new AgentStep(
                AgentStepType.USER_INPUT,
                "user",
                userMessage,
                userMessage
        ));

        String scratchpad = "";

        for (int step = 1; step <= options.getMaxSteps(); step++) {
            String prompt = buildPrompt(userMessage, scratchpad);

            ChatResponse response = chatModel.call(new ChatRequest()
                    .addMessage(ChatMessage.system(systemPrompt()))
                    .addMessage(ChatMessage.user(prompt)));

            String content = response.content();

            steps.add(new AgentStep(
                    AgentStepType.MODEL_RESPONSE,
                    "react_thought",
                    content,
                    response
            ));

            ReActAction action = parseAction(content);

            if (action.finalAnswer() != null) {
                steps.add(new AgentStep(
                        AgentStepType.FINAL_ANSWER,
                        "final_answer",
                        action.finalAnswer(),
                        action.finalAnswer()
                ));

                return new AgentResponse(action.finalAnswer(), steps);
            }

            if (action.toolName() == null || action.toolName().isBlank()) {
                String finalAnswer = content;

                steps.add(new AgentStep(
                        AgentStepType.FINAL_ANSWER,
                        "final_answer",
                        finalAnswer,
                        finalAnswer
                ));

                return new AgentResponse(finalAnswer, steps);
            }

            steps.add(new AgentStep(
                    AgentStepType.TOOL_CALL,
                    action.toolName(),
                    String.valueOf(action.arguments()),
                    action
            ));

            ToolResult toolResult = toolRegistry.execute(action.toolName(), action.arguments());

            steps.add(new AgentStep(
                    AgentStepType.TOOL_RESULT,
                    action.toolName(),
                    toolResult.success() ? toolResult.content() : toolResult.errorMessage(),
                    toolResult
            ));

            scratchpad += "\n" + content;
            scratchpad += "\nObservation: " + (toolResult.success() ? toolResult.content() : toolResult.errorMessage());
        }

        String answer = "Agent stopped because maxSteps was reached. maxSteps=" + options.getMaxSteps();

        steps.add(new AgentStep(
                AgentStepType.FINAL_ANSWER,
                "max_steps_reached",
                answer,
                answer
        ));

        return new AgentResponse(answer, steps);
    }

    private String systemPrompt() {
        return """
                你是一个 ReAct Agent。
                你必须严格按照下面格式回答：

                如果需要调用工具：
                Thought: 你的思考
                Action: 工具名称
                Action Input: 参数，支持 JSON，例如 {"city":"Beijing"}，也支持 key=value，多参数用英文逗号分隔

                如果已经得到最终答案：
                Thought: 你的思考
                Final Answer: 最终答案

                可用工具：
                %s

                注意：
                - Action 必须是工具名称
                - Action Input 推荐使用 JSON，例如 {"city":"Beijing"}
                - 如果不用 JSON，也可以使用 city=Beijing
                - 最终回答请使用中文
                """.formatted(toolDescriptions());
    }

    private String toolDescriptions() {
        StringBuilder builder = new StringBuilder();

        for (ToolDefinition definition : toolRegistry.list()) {
            builder.append("- ")
                    .append(definition.name())
                    .append(": ")
                    .append(definition.description())
                    .append("\n");

            for (ToolParameter parameter : definition.parameters()) {
                builder.append("  - ")
                        .append(parameter.name())
                        .append("，type=")
                        .append(parameter.type())
                        .append("，required=")
                        .append(parameter.required())
                        .append("，description=")
                        .append(parameter.description())
                        .append("\n");
            }
        }

        return builder.toString();
    }

    private String buildPrompt(String userMessage, String scratchpad) {
        return """
                用户问题：
                %s

                已有推理过程：
                %s

                请继续推理。如果需要工具，输出 Thought / Action / Action Input。
                如果已经可以回答，输出 Thought / Final Answer。
                """.formatted(userMessage, scratchpad == null ? "" : scratchpad);
    }

    private ReActAction parseAction(String content) {
        if (content == null || content.isBlank()) {
            return new ReActAction(null, Map.of(), null);
        }

        String finalAnswer = extractLineValue(content, "Final Answer:");
        if (finalAnswer != null && !finalAnswer.isBlank()) {
            return new ReActAction(null, Map.of(), finalAnswer);
        }

        String toolName = extractLineValue(content, "Action:");
        String input = extractLineValue(content, "Action Input:");

        if (toolName == null || toolName.isBlank()) {
            return new ReActAction(null, Map.of(), null);
        }

        return new ReActAction(toolName.trim(), parseArguments(input), null);
    }

    private String extractLineValue(String content, String prefix) {
        String[] lines = content.split("\\R");

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.startsWith(prefix)) {
                return trimmed.substring(prefix.length()).trim();
            }
        }

        return null;
    }

    private Map<String, Object> parseArguments(String input) {
        if (input == null || input.isBlank()) {
            return Map.of();
        }

        String trimmed = input.trim();

        if (options.isAllowJsonActionInput() && trimmed.startsWith("{") && trimmed.endsWith("}")) {
            try {
                return objectMapper.readValue(
                        trimmed,
                        new TypeReference<Map<String, Object>>() {
                        }
                );
            } catch (Exception ignored) {
                // fallback to key=value parser
            }
        }

        java.util.Map<String, Object> arguments = new java.util.LinkedHashMap<>();

        String[] pairs = trimmed.split(",");

        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);

            if (kv.length == 2) {
                arguments.put(kv[0].trim(), kv[1].trim());
            }
        }

        return arguments;
    }

    private record ReActAction(
            String toolName,
            Map<String, Object> arguments,
            String finalAnswer
    ) {
    }

    public static class Builder {

        private ChatModel chatModel;
        private ToolRegistry toolRegistry;
        private ReActAgentOptions options;

        public Builder chatModel(ChatModel chatModel) {
            this.chatModel = chatModel;
            return this;
        }

        public Builder toolRegistry(ToolRegistry toolRegistry) {
            this.toolRegistry = toolRegistry;
            return this;
        }

        public Builder maxSteps(int maxSteps) {
            this.options = ReActAgentOptions.builder()
                    .maxSteps(maxSteps)
                    .build();
            return this;
        }

        public Builder options(ReActAgentOptions options) {
            this.options = options;
            return this;
        }

        public ReActAgent build() {
            if (chatModel == null) {
                throw new IllegalArgumentException("chatModel is required");
            }

            if (toolRegistry == null) {
                throw new IllegalArgumentException("toolRegistry is required");
            }


            return new ReActAgent(this);
        }
    }
}