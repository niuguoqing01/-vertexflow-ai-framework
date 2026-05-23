package com.vertexflow.ai.core.tool;

public record AgentStep(
        AgentStepType type,
        String name,
        String content,
        Object data
) {
}