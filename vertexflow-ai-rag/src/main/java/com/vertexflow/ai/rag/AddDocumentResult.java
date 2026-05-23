package com.vertexflow.ai.rag;

public record AddDocumentResult(
        String documentId,
        int totalChunks,
        int addedChunks,
        int skippedChunks
) {
}