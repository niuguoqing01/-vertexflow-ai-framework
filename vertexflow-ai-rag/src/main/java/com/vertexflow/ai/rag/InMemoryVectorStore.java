package com.vertexflow.ai.rag;

import com.vertexflow.ai.core.embedding.EmbeddingModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InMemoryVectorStore {

    private record VectorItem(DocumentChunk chunk, double[] vector) {
    }

    public record SearchResult(DocumentChunk chunk, double score) {
    }

    private final EmbeddingModel embeddingModel;
    private final List<VectorItem> items = new ArrayList<>();

    public InMemoryVectorStore(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public void add(List<DocumentChunk> chunks) {
        for (DocumentChunk chunk : chunks) {
            double[] vector = embeddingModel.embed(chunk.content()).vector();
            items.add(new VectorItem(chunk, vector));
        }
    }

    public List<SearchResult> search(String query, int topK) {
        double[] queryVector = embeddingModel.embed(query).vector();

        return items.stream()
                .map(item -> new SearchResult(item.chunk(), cosine(queryVector, item.vector())))
                .sorted(Comparator.comparingDouble(SearchResult::score).reversed())
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