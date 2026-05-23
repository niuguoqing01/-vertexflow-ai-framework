package com.vertexflow.ai.core.chat;

public interface StreamingChatModel extends ChatModel {

    void stream(ChatRequest request, ChatStreamHandler handler);

    default void stream(String message, ChatStreamHandler handler) {
        ChatRequest request = new ChatRequest()
                .addMessage(ChatMessage.user(message));

        stream(request, handler);
    }
}
