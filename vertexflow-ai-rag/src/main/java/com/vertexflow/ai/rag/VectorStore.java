package com.vertexflow.ai.rag;

import java.util.List;

public interface VectorStore {

    void add(List<DocumentChunk> chunks);

    List<VectorSearchResult> search(String query, int topK);
}
