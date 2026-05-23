package com.vertexflow.ai.springexample;

public record RagStatusResponse(
        boolean enabled,
        int documentCount,
        int totalChunks,
        int addedChunks,
        int skippedChunks
) {
}