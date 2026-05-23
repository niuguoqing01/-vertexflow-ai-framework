package com.vertexflow.ai.rag;

import com.vertexflow.ai.core.embedding.EmbeddingModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InMemoryVectorStore implements VectorStore {

    private record VectorItem(DocumentChunk chunk, double[] vector) {
    }

    private final EmbeddingModel embeddingModel;
    private final List<VectorItem> items = new ArrayList<>();

    @Override
    public boolean exists(String chunkId) {
        if (chunkId == null || chunkId.isBlank()) {
            return false;
        }

        return items.stream()
                .anyMatch(item -> item.chunk().id().equals(chunkId));
    }

    public InMemoryVectorStore(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public void add(List<DocumentChunk> chunks) {
        for (DocumentChunk chunk : chunks) {
            double[] vector = embeddingModel.embed(chunk.content()).vector();
            items.add(new VectorItem(chunk, vector));
        }
    }

    @Override
    public List<VectorSearchResult> search(String query, int topK) {
        double[] queryVector = embeddingModel.embed(query).vector();

        return items.stream()
                .map(item -> new VectorSearchResult(item.chunk(), cosine(queryVector, item.vector())))
                .sorted(Comparator.comparingDouble(VectorSearchResult::score).reversed())
                .limit(topK)
                .toList();
    }

    private double cosine(double[] a, double[] b) {
        double result = 0.0;
        int length = Math.min(a.length, b.length);

        for (int i = 0; i < length; i++) {
            result += a[i] * b[i];
        }

        return result;
    }
}