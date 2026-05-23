package com.vertexflow.ai.rag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InMemoryVectorStore {

    private record VectorItem(DocumentChunk chunk, double[] vector) {
    }

    public record SearchResult(DocumentChunk chunk, double score) {
    }

    private final SimpleTextEmbedding embedding;
    private final List<VectorItem> items = new ArrayList<>();

    public InMemoryVectorStore(SimpleTextEmbedding embedding) {
        this.embedding = embedding;
    }

    public void add(List<DocumentChunk> chunks) {
        for (DocumentChunk chunk : chunks) {
            items.add(new VectorItem(chunk, embedding.embed(chunk.content())));
        }
    }

    public List<SearchResult> search(String query, int topK) {
        double[] queryVector = embedding.embed(query);

        return items.stream()
                .map(item -> new SearchResult(item.chunk(), cosine(queryVector, item.vector())))
                .sorted(Comparator.comparingDouble(SearchResult::score).reversed())
                .limit(topK)
                .toList();
    }

    private double cosine(double[] a, double[] b) {
        double result = 0.0;
        for (int i = 0; i < a.length; i++) {
            result += a[i] * b[i];
        }
        return result;
    }
}
