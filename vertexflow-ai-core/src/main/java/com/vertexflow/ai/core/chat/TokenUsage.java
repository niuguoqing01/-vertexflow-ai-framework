package com.vertexflow.ai.core.chat;

public record TokenUsage(
        Integer inputTokens,
        Integer outputTokens,
        Integer totalTokens
) {
    public static TokenUsage empty() {
        return new TokenUsage(null, null, null);
    }

    public static TokenUsage of(Integer inputTokens, Integer outputTokens) {
        Integer totalTokens = null;

        if (inputTokens != null && outputTokens != null) {
            totalTokens = inputTokens + outputTokens;
        }

        return new TokenUsage(inputTokens, outputTokens, totalTokens);
    }
}
