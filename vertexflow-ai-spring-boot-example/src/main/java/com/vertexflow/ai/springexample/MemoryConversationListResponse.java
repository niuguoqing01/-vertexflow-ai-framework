package com.vertexflow.ai.springexample;

import java.util.List;

public record MemoryConversationListResponse(
        boolean success,
        String implementation,
        int conversationCount,
        List<String> conversationIds,
        String message
) {
}