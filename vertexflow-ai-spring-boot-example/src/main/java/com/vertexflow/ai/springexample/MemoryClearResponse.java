package com.vertexflow.ai.springexample;

public record MemoryClearResponse(
        boolean success,
        String conversationId,
        String message
) {
}