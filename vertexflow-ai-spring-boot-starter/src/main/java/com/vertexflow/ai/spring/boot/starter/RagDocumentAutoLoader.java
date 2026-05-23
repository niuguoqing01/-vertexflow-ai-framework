package com.vertexflow.ai.spring.boot.starter;

import com.vertexflow.ai.rag.DirectoryDocumentLoader;
import com.vertexflow.ai.rag.Document;
import com.vertexflow.ai.rag.RagEngine;
import com.vertexflow.ai.rag.UrlDocumentLoader;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import com.vertexflow.ai.rag.AddDocumentResult;

import java.util.ArrayList;
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
        List<Document> documents = new ArrayList<>();

        loadLocalDocuments(documents);
        loadUrlDocuments(documents);

        int totalChunks = 0;
        int addedChunks = 0;
        int skippedChunks = 0;

        for (Document document : documents) {
            AddDocumentResult result = ragEngine.addDocumentWithResult(document);

            totalChunks += result.totalChunks();
            addedChunks += result.addedChunks();
            skippedChunks += result.skippedChunks();

            System.out.println("[VertexFlow AI] RAG document loaded: "
                    + result.documentId()
                    + ", totalChunks=" + result.totalChunks()
                    + ", addedChunks=" + result.addedChunks()
                    + ", skippedChunks=" + result.skippedChunks());
        }

        if (!documents.isEmpty()) {
            System.out.println("[VertexFlow AI] Auto loaded RAG documents.");
            System.out.println("[VertexFlow AI] Document count: " + documents.size());
            System.out.println("[VertexFlow AI] Total chunks: " + totalChunks);
            System.out.println("[VertexFlow AI] Added chunks: " + addedChunks);
            System.out.println("[VertexFlow AI] Skipped chunks: " + skippedChunks);
        }
    }

    private void loadLocalDocuments(List<Document> documents) {
        String documentLocation = properties.getRag().getDocumentLocation();

        if (documentLocation == null || documentLocation.isBlank()) {
            return;
        }

        List<Document> localDocuments = DirectoryDocumentLoader.loadDirectory(documentLocation);
        documents.addAll(localDocuments);

        System.out.println("[VertexFlow AI] Loaded local RAG documents from: " + documentLocation);
        System.out.println("[VertexFlow AI] Local document count: " + localDocuments.size());
    }

    private void loadUrlDocuments(List<Document> documents) {
        List<String> urls = properties.getRag().getDocumentUrls();

        if (urls == null || urls.isEmpty()) {
            return;
        }

        int successCount = 0;

        for (String url : urls) {
            if (url == null || url.isBlank()) {
                continue;
            }

            Document document = UrlDocumentLoader.load(url);
            documents.add(document);
            successCount++;
        }

        System.out.println("[VertexFlow AI] Loaded URL RAG documents.");
        System.out.println("[VertexFlow AI] URL document count: " + successCount);
    }
}