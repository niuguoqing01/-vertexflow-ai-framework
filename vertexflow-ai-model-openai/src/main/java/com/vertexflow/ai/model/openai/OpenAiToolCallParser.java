package com.vertexflow.ai.model.openai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vertexflow.ai.core.exception.AiErrorCode;
import com.vertexflow.ai.core.exception.AiException;
import com.vertexflow.ai.core.tool.ToolCall;
import com.vertexflow.ai.core.tool.ToolCallParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenAiToolCallParser implements ToolCallParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<ToolCall> parse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return List.of();
        }

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode choices = root.path("choices");

            if (!choices.isArray() || choices.isEmpty()) {
                return List.of();
            }

            JsonNode toolCallsNode = choices.get(0)
                    .path("message")
                    .path("tool_calls");

            if (!toolCallsNode.isArray() || toolCallsNode.isEmpty()) {
                return List.of();
            }

            List<ToolCall> toolCalls = new ArrayList<>();

            for (JsonNode toolCallNode : toolCallsNode) {
                JsonNode functionNode = toolCallNode.path("function");

                String name = functionNode.path("name").asText(null);
                String argumentsJson = functionNode.path("arguments").asText("{}");

                if (name == null || name.isBlank()) {
                    continue;
                }

                Map<String, Object> arguments = objectMapper.readValue(
                        argumentsJson,
                        new TypeReference<Map<String, Object>>() {
                        }
                );

                toolCalls.add(new ToolCall(name, arguments));
            }

            return toolCalls;
        } catch (Exception e) {
            throw new AiException(
                    AiErrorCode.INVALID_REQUEST,
                    "Failed to parse OpenAI tool calls",
                    e
            );
        }
    }
}
