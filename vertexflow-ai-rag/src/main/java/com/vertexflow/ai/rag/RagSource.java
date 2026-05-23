package com.vertexflow.ai.rag;

public record RagSource(
        String chunkId,
        String documentId,
        String content,
        double score
) {
}
