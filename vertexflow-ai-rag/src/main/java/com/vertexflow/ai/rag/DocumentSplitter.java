package com.vertexflow.ai.rag;

import java.util.ArrayList;
import java.util.List;

public class DocumentSplitter {

    private final int chunkSize;
    private final int overlap;

    public DocumentSplitter(int chunkSize, int overlap) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be greater than 0");
        }
        if (overlap < 0 || overlap >= chunkSize) {
            throw new IllegalArgumentException("overlap must be >= 0 and < chunkSize");
        }
        this.chunkSize = chunkSize;
        this.overlap = overlap;
    }

    public List<DocumentChunk> split(Document document) {
        List<DocumentChunk> chunks = new ArrayList<>();
        String text = document.content();

        int start = 0;
        int index = 0;

        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            String content = text.substring(start, end);
            chunks.add(new DocumentChunk(document.id() + "_chunk_" + index, document.id(), content));
            index++;

            if (end == text.length()) {
                break;
            }

            start = end - overlap;
        }

        return chunks;
    }
}
