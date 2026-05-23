package com.vertexflow.ai.springexample;

public record RagReloadResponse(
        boolean success,
        int documentCount,
        int totalChunks,
        int addedChunks,
        int skippedChunks,
        String message
) {
}