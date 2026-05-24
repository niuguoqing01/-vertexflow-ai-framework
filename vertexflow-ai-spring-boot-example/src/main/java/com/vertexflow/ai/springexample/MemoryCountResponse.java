package com.vertexflow.ai.springexample;

public record MemoryCountResponse(
        boolean success,
        String conversationId,
        int messageCount,
        String message
) {
}