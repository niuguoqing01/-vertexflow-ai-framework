package com.vertexflow.ai.core.chat;

public record ChatResponse(
        String content,
        String model,
        TokenUsage usage,
        String finishReason,
        String rawResponse
) {
    public ChatResponse(String content, String model, Integer inputTokens, Integer outputTokens) {
        this(content, model, TokenUsage.of(inputTokens, outputTokens), null, null);
    }

    public static ChatResponse of(String content, String model) {
        return new ChatResponse(content, model, TokenUsage.empty(), null, null);
    }
}