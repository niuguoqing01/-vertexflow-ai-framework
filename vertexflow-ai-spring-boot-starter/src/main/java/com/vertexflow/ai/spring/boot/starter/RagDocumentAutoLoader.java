package com.vertexflow.ai.spring.boot.starter;

import com.vertexflow.ai.rag.DirectoryDocumentLoader;
import com.vertexflow.ai.rag.Document;
import com.vertexflow.ai.rag.RagEngine;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.List;

public class RagDocumentAutoLoader implements ApplicationRunner {

    private final RagEngine ragEngine;
    private final VertexFlowAiProperties properties;

    public RagDocumentAutoLoader(RagEngine ragEngine, VertexFlowAiProperties properties) {
        this.ragEngine = ragEngine;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        String documentLocation = properties.getRag().getDocumentLocation();

        if (documentLocation == null || documentLocation.isBlank()) {
            return;
        }

        List<Document> documents = DirectoryDocumentLoader.loadDirectory(documentLocation);

        for (Document document : documents) {
            ragEngine.addDocument(document);
        }

        System.out.println("[VertexFlow AI] Loaded RAG documents from: " + documentLocation);
        System.out.println("[VertexFlow AI] Document count: " + documents.size());
    }
}