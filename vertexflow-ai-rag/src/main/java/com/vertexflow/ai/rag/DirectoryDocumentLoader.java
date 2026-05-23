package com.vertexflow.ai.rag;

import com.vertexflow.ai.core.exception.AiErrorCode;
import com.vertexflow.ai.core.exception.AiException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class DirectoryDocumentLoader {

    private static final Set<String> DEFAULT_EXTENSIONS = Set.of(".txt", ".md", ".pdf");

    private DirectoryDocumentLoader() {
    }

    public static List<Document> loadDirectory(String directoryPath) {
        return loadDirectory(directoryPath, DEFAULT_EXTENSIONS, true);
    }

    public static List<Document> loadDirectory(String directoryPath, Set<String> extensions, boolean recursive) {
        try {
            Path root = Path.of(directoryPath);

            if (!Files.exists(root)) {
                throw new AiException(
                        AiErrorCode.DOCUMENT_LOAD_ERROR,
                        "Directory does not exist: " + directoryPath
                );
            }

            if (!Files.isDirectory(root)) {
                throw new AiException(
                        AiErrorCode.DOCUMENT_LOAD_ERROR,
                        "Path is not a directory: " + directoryPath
                );
            }

            int maxDepth = recursive ? Integer.MAX_VALUE : 1;

            try (var stream = Files.walk(root, maxDepth)) {
                return stream
                        .filter(Files::isRegularFile)
                        .filter(path -> isSupported(path, extensions))
                        .map(DirectoryDocumentLoader::toDocument)
                        .toList();
            }
        } catch (IOException e) {
            throw new AiException(
                    AiErrorCode.DOCUMENT_LOAD_ERROR,
                    "Failed to load directory: " + directoryPath,
                    e
            );
        }
    }

    private static boolean isSupported(Path path, Set<String> extensions) {
        String fileName = path.getFileName().toString().toLowerCase();

        for (String extension : extensions) {
            if (fileName.endsWith(extension.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    private static Document toDocument(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".pdf")) {
            return PdfDocumentLoader.load(path);
        }

        if (fileName.endsWith(".txt") || fileName.endsWith(".md")) {
            return TextFileDocumentLoader.loadFile(path.toString());
        }

        throw new AiException(
                AiErrorCode.DOCUMENT_LOAD_ERROR,
                "Unsupported document type: " + path
        );
    }
}