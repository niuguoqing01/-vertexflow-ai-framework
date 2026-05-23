package com.vertexflow.ai.rag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import com.vertexflow.ai.core.exception.AiErrorCode;
import com.vertexflow.ai.core.exception.AiException;

public class DirectoryDocumentLoader {

    private static final Set<String> DEFAULT_EXTENSIONS = Set.of(".txt", ".md");

    private DirectoryDocumentLoader() {
    }

    public static List<Document> loadDirectory(String directoryPath) {
        return loadDirectory(directoryPath, DEFAULT_EXTENSIONS, true);
    }

    public static List<Document> loadDirectory(String directoryPath, Set<String> extensions, boolean recursive) {
        try {
            Path root = Path.of(directoryPath);

            if (!Files.exists(root)) {
                throw new IllegalArgumentException("Directory does not exist: " + directoryPath);
            }

            if (!Files.isDirectory(root)) {
                throw new IllegalArgumentException("Path is not a directory: " + directoryPath);
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
            throw new AiException(AiErrorCode.DOCUMENT_LOAD_ERROR, "Failed to load directory: " + directoryPath, e);
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
        return TextFileDocumentLoader.loadFile(path.toString());
    }
}
