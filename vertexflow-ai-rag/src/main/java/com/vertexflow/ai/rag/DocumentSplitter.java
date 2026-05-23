package com.vertexflow.ai.rag;

import java.util.List;

public interface DocumentSplitter {

    List<DocumentChunk> split(Document document);
}