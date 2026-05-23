package com.vertexflow.ai.rag;

import com.vertexflow.ai.core.chat.ChatMessage;
import com.vertexflow.ai.core.chat.ChatModel;
import com.vertexflow.ai.core.chat.ChatRequest;
import com.vertexflow.ai.core.embedding.EmbeddingModel;

import java.util.List;
import java.util.stream.Collectors;

public class RagEngine {

    private final ChatModel chatModel;
    private final DocumentSplitter splitter;
    private final VectorStore vectorStore;
    private final RagOptions options;

    public RagEngine(ChatModel chatModel) {
        this(chatModel, new SimpleTextEmbedding(256));
    }

    public RagEngine(ChatModel chatModel, EmbeddingModel embeddingModel) {
        this(
                chatModel,
                new InMemoryVectorStore(embeddingModel),
                new FixedSizeDocumentSplitter(300, 50),
                RagOptions.defaults()
        );
    }

    public RagEngine(ChatModel chatModel, VectorStore vectorStore) {
        this(
                chatModel,
                vectorStore,
                new FixedSizeDocumentSplitter(300, 50),
                RagOptions.defaults()
        );
    }

    public RagEngine(ChatModel chatModel, VectorStore vectorStore, RagOptions options) {
        this(
                chatModel,
                vectorStore,
                new FixedSizeDocumentSplitter(300, 50),
                options
        );
    }

    public RagEngine(
            ChatModel chatModel,
            VectorStore vectorStore,
            DocumentSplitter splitter,
            RagOptions options
    ) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
        this.splitter = splitter == null ? new FixedSizeDocumentSplitter(300, 50) : splitter;
        this.options = options == null ? RagOptions.defaults() : options;
    }

    public void addDocument(Document document) {
        List<DocumentChunk> chunks = splitter.split(document);

        List<DocumentChunk> newChunks = chunks.stream()
                .filter(chunk -> !vectorStore.exists(chunk.id()))
                .toList();

        if (newChunks.isEmpty()) {
            return;
        }

        vectorStore.add(newChunks);
    }

    public String ask(String question) {
        return askWithSources(question).content();
    }

    public RagAnswer askWithSources(String question) {
        List<VectorSearchResult> results = vectorStore.search(question, options.getTopK());

        String context = results.stream()
                .map(result -> "- " + result.chunk().content())
                .collect(Collectors.joining("\n"));

        String prompt = """
                Please answer the user's question based on the context below.
                If the answer is not in the context, say you do not know. Do not make things up.

                Context:
                %s

                Question:
                %s
                """.formatted(context, question);

        ChatRequest request = new ChatRequest()
                .addMessage(ChatMessage.system("You are the RAG assistant of VertexFlow AI Framework."))
                .addMessage(ChatMessage.user(prompt));

        String answer = chatModel.call(request).content();

        List<RagSource> sources = options.isReturnSources()
                ? results.stream()
                .map(result -> new RagSource(
                        result.chunk().id(),
                        result.chunk().documentId(),
                        result.chunk().content(),
                        result.score()
                ))
                .toList()
                : List.of();

        return new RagAnswer(answer, sources, context);
    }
}