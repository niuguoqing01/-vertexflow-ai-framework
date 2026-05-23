package com.vertexflow.ai.core.chat;

public record ChatResponse(String content, String model, Integer inputTokens, Integer outputTokens) {
}
