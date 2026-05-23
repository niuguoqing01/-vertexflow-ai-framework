package com.vertexflow.ai.core.tool;

import com.vertexflow.ai.core.exception.AiErrorCode;
import com.vertexflow.ai.core.exception.AiException;

import java.util.ArrayList;
import java.util.List;

public class ToolCallExecutor {

    private final ToolRegistry registry;

    private ToolCallExecutor(ToolRegistry registry) {
        if (registry == null) {
            throw new AiException(AiErrorCode.INVALID_REQUEST, "ToolRegistry must not be null");
        }

        this.registry = registry;
    }

    public static ToolCallExecutor create(ToolRegistry registry) {
        return new ToolCallExecutor(registry);
    }

    public ToolCallResult execute(ToolCall toolCall) {
        if (toolCall == null) {
            throw new AiException(AiErrorCode.INVALID_REQUEST, "ToolCall must not be null");
        }

        ToolResult result = registry.execute(toolCall.name(), toolCall.arguments());

        return new ToolCallResult(toolCall, result);
    }

    public List<ToolCallResult> executeAll(List<ToolCall> toolCalls) {
        if (toolCalls == null || toolCalls.isEmpty()) {
            return List.of();
        }

        List<ToolCallResult> results = new ArrayList<>();

        for (ToolCall toolCall : toolCalls) {
            results.add(execute(toolCall));
        }

        return results;
    }
}
