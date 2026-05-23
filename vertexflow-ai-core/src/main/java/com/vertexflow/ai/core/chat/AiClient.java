package com.vertexflow.ai.core.chat;

import com.vertexflow.ai.core.exception.AiErrorCode;
import com.vertexflow.ai.core.exception.AiException;
import com.vertexflow.ai.core.memory.ChatMemory;

import java.util.ArrayList;
import java.util.List;

public class AiClient {

    private final ChatModel chatModel;
    private final List<ChatMessage> systemMessages;
    private final ChatMemory memory;
    private final String conversationId;

    private AiClient(Builder builder) {
        this.chatModel = builder.chatModel;
        this.systemMessages = new ArrayList<>(builder.systemMessages);
        this.memory = builder.memory;
        this.conversationId = builder.conversationId;
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
                .memory(this.memory)
                .conversationId(this.conversationId)
                .build();
    }

    public String chat(String userMessage) {
        ChatRequest request = createRequest(userMessage);
        ChatResponse response = chatModel.call(request);

        saveMemory(userMessage, response.content());

        return response.content();
    }

    public ChatResponse call(String userMessage) {
        ChatRequest request = createRequest(userMessage);
        ChatResponse response = chatModel.call(request);

        saveMemory(userMessage, response.content());

        return response;
    }

    public ChatResponse call(ChatRequest request) {
        ChatRequest finalRequest = mergeSystemMessagesAndMemory(request);
        ChatResponse response = chatModel.call(finalRequest);

        saveMemory(request, response.content());

        return response;
    }

    public void stream(String userMessage, ChatStreamHandler handler) {
        if (!(chatModel instanceof StreamingChatModel streamingChatModel)) {
            throw new AiException(AiErrorCode.UNSUPPORTED_OPERATION, "Current chatModel does not support streaming");
        }

        ChatRequest request = createRequest(userMessage);
        StringBuilder fullContent = new StringBuilder();

        streamingChatModel.stream(request, response -> {
            if (response.content() != null) {
                fullContent.append(response.content());
            }

            handler.onMessage(response);

            if (response.finished()) {
                saveMemory(userMessage, fullContent.toString());
            }
        });
    }

    public void stream(ChatRequest request, ChatStreamHandler handler) {
        if (!(chatModel instanceof StreamingChatModel streamingChatModel)) {
            throw new AiException(AiErrorCode.UNSUPPORTED_OPERATION, "Current chatModel does not support streaming");
        }

        ChatRequest finalRequest = mergeSystemMessagesAndMemory(request);
        StringBuilder fullContent = new StringBuilder();

        streamingChatModel.stream(finalRequest, response -> {
            if (response.content() != null) {
                fullContent.append(response.content());
            }

            handler.onMessage(response);

            if (response.finished()) {
                saveMemory(request, fullContent.toString());
            }
        });
    }

    private ChatRequest createRequest(String userMessage) {
        ChatRequest request = new ChatRequest();

        for (ChatMessage message : systemMessages) {
            request.addMessage(message);
        }

        addMemoryMessages(request);

        request.addMessage(ChatMessage.user(userMessage));
        return request;
    }

    private ChatRequest mergeSystemMessagesAndMemory(ChatRequest request) {
        ChatRequest finalRequest = new ChatRequest()
                .setOptions(request.getOptions());

        for (ChatMessage message : systemMessages) {
            finalRequest.addMessage(message);
        }

        addMemoryMessages(finalRequest);

        for (ChatMessage message : request.getMessages()) {
            finalRequest.addMessage(message);
        }

        return finalRequest;
    }

    private void addMemoryMessages(ChatRequest request) {
        if (memory == null || conversationId == null || conversationId.isBlank()) {
            return;
        }

        for (ChatMessage message : memory.get(conversationId)) {
            request.addMessage(message);
        }
    }

    private void saveMemory(String userMessage, String assistantMessage) {
        if (memory == null || conversationId == null || conversationId.isBlank()) {
            return;
        }

        memory.add(conversationId, ChatMessage.user(userMessage));
        memory.add(conversationId, ChatMessage.assistant(assistantMessage));
    }

    private void saveMemory(ChatRequest request, String assistantMessage) {
        if (memory == null || conversationId == null || conversationId.isBlank()) {
            return;
        }

        for (ChatMessage message : request.getMessages()) {
            if (message.role() == Role.USER || message.role() == Role.ASSISTANT) {
                memory.add(conversationId, message);
            }
        }

        memory.add(conversationId, ChatMessage.assistant(assistantMessage));
    }

    public static class Builder {

        private ChatModel chatModel;
        private final List<ChatMessage> systemMessages = new ArrayList<>();
        private ChatMemory memory;
        private String conversationId;

        public Builder chatModel(ChatModel chatModel) {
            this.chatModel = chatModel;
            return this;
        }

        public Builder system(String content) {
            this.systemMessages.add(ChatMessage.system(content));
            return this;
        }

        public Builder memory(ChatMemory memory) {
            this.memory = memory;
            return this;
        }

        public Builder conversationId(String conversationId) {
            this.conversationId = conversationId;
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