package com.vertexflow.ai.rag;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TextFileDocumentLoader implements DocumentLoader {

    @Override
    public Document load(String path) {
        return loadFile(path);
    }

    public static Document loadFile(String path) {
        try {
            Path filePath = Path.of(path);
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            return new Document(filePath.getFileName().toString(), content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load text file: " + path, e);
        }
    }
}
