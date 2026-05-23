package com.vertexflow.ai.core.chat;

import java.util.ArrayList;
import java.util.List;

public class AiClient {

    private final ChatModel chatModel;
    private final List<ChatMessage> systemMessages;

    private AiClient(Builder builder) {
        this.chatModel = builder.chatModel;
        this.systemMessages = new ArrayList<>(builder.systemMessages);
    }

    public static AiClient create(ChatModel chatModel) {
        return AiClient.builder()
                .chatModel(chatModel)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public AiClient system(String content) {
        List<ChatMessage> newSystemMessages = new ArrayList<>(this.systemMessages);
        newSystemMessages.add(ChatMessage.system(content));

        return AiClient.builder()
                .chatModel(this.chatModel)
                .systemMessages(newSystemMessages)
                .build();
    }

    public String chat(String userMessage) {
        ChatRequest request = createRequest(userMessage);
        return chatModel.call(request).content();
    }

    public ChatResponse call(String userMessage) {
        ChatRequest request = createRequest(userMessage);
        return chatModel.call(request);
    }

    public ChatResponse call(ChatRequest request) {
        ChatRequest finalRequest = mergeSystemMessages(request);
        return chatModel.call(finalRequest);
    }

    public void stream(String userMessage, ChatStreamHandler handler) {
        if (!(chatModel instanceof StreamingChatModel streamingChatModel)) {
            throw new UnsupportedOperationException("Current chatModel does not support streaming");
        }

        ChatRequest request = createRequest(userMessage);
        streamingChatModel.stream(request, handler);
    }

    public void stream(ChatRequest request, ChatStreamHandler handler) {
        if (!(chatModel instanceof StreamingChatModel streamingChatModel)) {
            throw new UnsupportedOperationException("Current chatModel does not support streaming");
        }

        ChatRequest finalRequest = mergeSystemMessages(request);
        streamingChatModel.stream(finalRequest, handler);
    }

    private ChatRequest createRequest(String userMessage) {
        ChatRequest request = new ChatRequest();

        for (ChatMessage message : systemMessages) {
            request.addMessage(message);
        }

        request.addMessage(ChatMessage.user(userMessage));
        return request;
    }

    private ChatRequest mergeSystemMessages(ChatRequest request) {
        ChatRequest finalRequest = new ChatRequest()
                .setOptions(request.getOptions());

        for (ChatMessage message : systemMessages) {
            finalRequest.addMessage(message);
        }

        for (ChatMessage message : request.getMessages()) {
            finalRequest.addMessage(message);
        }

        return finalRequest;
    }

    public static class Builder {

        private ChatModel chatModel;
        private final List<ChatMessage> systemMessages = new ArrayList<>();

        public Builder chatModel(ChatModel chatModel) {
            this.chatModel = chatModel;
            return this;
        }

        public Builder system(String content) {
            this.systemMessages.add(ChatMessage.system(content));
            return this;
        }

        private Builder systemMessages(List<ChatMessage> messages) {
            this.systemMessages.clear();
            this.systemMessages.addAll(messages);
            return this;
        }

        public AiClient build() {
            if (chatModel == null) {
                throw new IllegalArgumentException("chatModel is required");
            }

            return new AiClient(this);
        }
    }
}