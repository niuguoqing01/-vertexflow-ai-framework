package com.vertexflow.ai.springexample;

public record RagDeleteResponse(
        boolean success,
        String documentId,
        int deletedChunks,
        String message
) {
}