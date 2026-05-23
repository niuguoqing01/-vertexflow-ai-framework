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
    private final InMemoryVectorStore vectorStore;

    public RagEngine(ChatModel chatModel) {
        this(chatModel, new SimpleTextEmbedding(256));
    }

    public RagEngine(ChatModel chatModel, EmbeddingModel embeddingModel) {
        this.chatModel = chatModel;
        this.splitter = new DocumentSplitter(300, 50);
        this.vectorStore = new InMemoryVectorStore(embeddingModel);
    }

    public void addDocument(Document document) {
        List<DocumentChunk> chunks = splitter.split(document);
        vectorStore.add(chunks);
    }

    public String ask(String question) {
        List<InMemoryVectorStore.SearchResult> results = vectorStore.search(question, 3);

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

        return chatModel.call(request).content();
    }
}