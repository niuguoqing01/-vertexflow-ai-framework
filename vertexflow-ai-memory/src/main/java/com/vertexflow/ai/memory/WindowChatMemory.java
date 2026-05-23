package com.vertexflow.ai.memory;

import com.vertexflow.ai.core.chat.ChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WindowChatMemory implements ChatMemory {

    private final int maxMessages;
    private final Map<String, List<ChatMessage>> store = new HashMap<>();

    public WindowChatMemory(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    @Override
    public void add(String conversationId, ChatMessage message) {
        List<ChatMessage> messages = store.computeIfAbsent(conversationId, key -> new ArrayList<>());
        messages.add(message);

        while (messages.size() > maxMessages) {
            messages.remove(0);
        }
    }

    @Override
    public List<ChatMessage> get(String conversationId) {
        return new ArrayList<>(store.getOrDefault(conversationId, List.of()));
    }

    @Override
    public void clear(String conversationId) {
        store.remove(conversationId);
    }
}
