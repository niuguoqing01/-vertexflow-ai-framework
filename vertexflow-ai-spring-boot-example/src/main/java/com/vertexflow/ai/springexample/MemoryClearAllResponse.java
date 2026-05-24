package com.vertexflow.ai.springexample;

public record MemoryClearAllResponse(
        boolean success,
        int deletedMessages,
        String message
) {
}