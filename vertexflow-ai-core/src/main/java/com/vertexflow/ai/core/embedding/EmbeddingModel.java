package com.vertexflow.ai.core.embedding;

public interface EmbeddingModel {

    EmbeddingResponse embed(EmbeddingRequest request);

    default EmbeddingResponse embed(String text) {
        return embed(new EmbeddingRequest(text));
    }
}
