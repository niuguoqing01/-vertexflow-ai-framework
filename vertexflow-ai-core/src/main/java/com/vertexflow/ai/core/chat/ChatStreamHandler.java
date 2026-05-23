package com.vertexflow.ai.core.chat;

@FunctionalInterface
public interface ChatStreamHandler {

    void onMessage(StreamChatResponse response);
}
