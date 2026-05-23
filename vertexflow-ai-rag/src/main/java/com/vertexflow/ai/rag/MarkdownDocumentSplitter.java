package com.vertexflow.ai.rag;

import java.util.ArrayList;
import java.util.List;

public class MarkdownDocumentSplitter implements DocumentSplitter {

    private final int maxChunkSize;
    private final int overlap;
    private final FixedSizeDocumentSplitter fallbackSplitter;

    public MarkdownDocumentSplitter(int maxChunkSize, int overlap) {
        if (maxChunkSize <= 0) {
            throw new IllegalArgumentException("maxChunkSize must be greater than 0");
        }
        if (overlap < 0 || overlap >= maxChunkSize) {
            throw new IllegalArgumentException("overlap must be >= 0 and < maxChunkSize");
        }

        this.maxChunkSize = maxChunkSize;
        this.overlap = overlap;
        this.fallbackSplitter = new FixedSizeDocumentSplitter(maxChunkSize, overlap);
    }

    @Override
    public List<DocumentChunk> split(Document document) {
        String content = document.content();

        if (content == null || content.isBlank()) {
            return List.of();
        }

        List<String> sections = splitByMarkdownHeadings(content);

        if (sections.size() <= 1) {
            return fallbackSplitter.split(document);
        }

        List<DocumentChunk> chunks = new ArrayList<>();
        int index = 0;

        for (String section : sections) {
            if (section.isBlank()) {
                continue;
            }

            if (section.length() <= maxChunkSize) {
                chunks.add(new DocumentChunk(
                        document.id() + "_chunk_" + index,
                        document.id(),
                        section.trim()
                ));
                index++;
            } else {
                Document tempDocument = new Document(
                        document.id() + "_section_" + index,
                        section
                );

                List<DocumentChunk> subChunks = fallbackSplitter.split(tempDocument);

                for (DocumentChunk subChunk : subChunks) {
                    chunks.add(new DocumentChunk(
                            document.id() + "_chunk_" + index,
                            document.id(),
                            subChunk.content()
                    ));
                    index++;
                }
            }
        }

        return chunks;
    }

    private List<String> splitByMarkdownHeadings(String content) {
        List<String> sections = new ArrayList<>();

        String[] lines = content.split("\\R");
        StringBuilder current = new StringBuilder();

        for (String line : lines) {
            if (isHeading(line) && !current.isEmpty()) {
                sections.add(current.toString().trim());
                current.setLength(0);
            }

            current.append(line).append(System.lineSeparator());
        }

        if (!current.isEmpty()) {
            sections.add(current.toString().trim());
        }

        return sections;
    }

    private boolean isHeading(String line) {
        String trimmed = line.trim();

        if (!trimmed.startsWith("#")) {
            return false;
        }

        int count = 0;
        while (count < trimmed.length() && trimmed.charAt(count) == '#') {
            count++;
        }

        return count >= 1
                && count <= 6
                && count < trimmed.length()
                && Character.isWhitespace(trimmed.charAt(count));
    }
}