package com.vertexflow.ai.springexample;

import java.util.List;

public record MemoryMessagesResponse(
        String conversationId,
        int messageCount,
        List<MemoryMessageResponse> messages
) {
}