package com.vertexflow.ai.core.chat;

public interface ChatModel {

    ChatResponse call(ChatRequest request);

    default String chat(String message) {
        ChatRequest request = new ChatRequest()
                .addMessage(ChatMessage.user(message));
        return call(request).content();
    }
}
