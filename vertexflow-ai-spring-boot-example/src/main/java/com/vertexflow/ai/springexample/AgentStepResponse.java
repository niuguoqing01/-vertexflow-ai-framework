package com.vertexflow.ai.springexample;

public record AgentStepResponse(
        String type,
        String name,
        String content
) {
}