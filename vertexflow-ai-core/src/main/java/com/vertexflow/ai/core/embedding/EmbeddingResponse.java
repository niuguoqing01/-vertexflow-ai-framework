package com.vertexflow.ai.core.embedding;

public record EmbeddingResponse(double[] vector, String model, Integer tokens) {
}
