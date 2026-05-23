package com.vertexflow.ai.core.memory;

import com.vertexflow.ai.core.chat.ChatMessage;

import java.util.List;

public interface ChatMemory {

    void add(String conversationId, ChatMessage message);

    List<ChatMessage> get(String conversationId);

    void clear(String conversationId);
}
