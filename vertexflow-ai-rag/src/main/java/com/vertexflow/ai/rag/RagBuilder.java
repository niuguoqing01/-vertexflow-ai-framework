package com.vertexflow.ai.rag;

import com.vertexflow.ai.core.chat.ChatModel;
import com.vertexflow.ai.core.embedding.EmbeddingModel;

public class RagBuilder {

    private ChatModel chatModel;
    private EmbeddingModel embeddingModel;
    private VectorStore vectorStore;
    private DocumentSplitter splitter;
    private RagOptions options;

    private RagBuilder() {
    }

    public static RagBuilder create() {
        return new RagBuilder();
    }

    public RagBuilder chatModel(ChatModel chatModel) {
        this.chatModel = chatModel;
        return this;
    }

    public RagBuilder embeddingModel(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
        return this;
    }

    public RagBuilder vectorStore(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        return this;
    }

    public RagBuilder splitter(DocumentSplitter splitter) {
        this.splitter = splitter;
        return this;
    }

    public RagBuilder options(RagOptions options) {
        this.options = options;
        return this;
    }

    public RagEngine build() {
        if (chatModel == null) {
            throw new IllegalArgumentException("chatModel is required");
        }

        RagOptions finalOptions = options == null ? RagOptions.defaults() : options;
        DocumentSplitter finalSplitter = splitter == null
                ? new FixedSizeDocumentSplitter(300, 50)
                : splitter;

        VectorStore finalVectorStore = vectorStore;

        if (finalVectorStore == null) {
            EmbeddingModel finalEmbeddingModel = embeddingModel == null
                    ? new SimpleTextEmbedding(256)
                    : embeddingModel;

            finalVectorStore = new InMemoryVectorStore(finalEmbeddingModel);
        }

        return new RagEngine(
                chatModel,
                finalVectorStore,
                finalSplitter,
                finalOptions
        );
    }
}