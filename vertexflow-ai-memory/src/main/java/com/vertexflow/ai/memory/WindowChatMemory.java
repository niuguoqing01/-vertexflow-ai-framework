package com.vertexflow.ai.memory;

import com.vertexflow.ai.core.chat.ChatMessage;
import com.vertexflow.ai.core.chat.Role;
import com.vertexflow.ai.core.memory.ChatMemory;
import com.vertexflow.ai.core.memory.MemoryOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WindowChatMemory implements ChatMemory {

    private final MemoryOptions options;
    private final Map<String, List<ChatMessage>> store = new HashMap<>();

    public WindowChatMemory(int maxMessages) {
        this(MemoryOptions.builder()
                .maxMessages(maxMessages)
                .build());
    }

    public WindowChatMemory(MemoryOptions options) {
        this.options = options == null ? MemoryOptions.defaults() : options;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void add(String conversationId, ChatMessage message) {
        List<ChatMessage> messages = store.computeIfAbsent(conversationId, key -> new ArrayList<>());
        messages.add(message);

        trim(messages);
    }

    @Override
    public List<ChatMessage> get(String conversationId) {
        return new ArrayList<>(store.getOrDefault(conversationId, List.of()));
    }

    @Override
    public void clear(String conversationId) {
        store.remove(conversationId);
    }

    private void trim(List<ChatMessage> messages) {
        while (countTrimTargetMessages(messages) > options.getMaxMessages()) {
            int removeIndex = findFirstRemovableIndex(messages);

            if (removeIndex < 0) {
                break;
            }

            messages.remove(removeIndex);
        }
    }

    private int countTrimTargetMessages(List<ChatMessage> messages) {
        if (options.isKeepSystemMessage()) {
            return messages.size();
        }

        int count = 0;
        for (ChatMessage message : messages) {
            if (message.role() != Role.SYSTEM) {
                count++;
            }
        }

        return count;
    }

    private int findFirstRemovableIndex(List<ChatMessage> messages) {
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage message = messages.get(i);

            if (options.isKeepSystemMessage() && message.role() == Role.SYSTEM) {
                return i;
            }

            if (!options.isKeepSystemMessage() && message.role() != Role.SYSTEM) {
                return i;
            }
        }

        return -1;
    }

    public static class Builder {

        private int maxMessages = 10;
        private boolean keepSystemMessage = false;

        public Builder maxMessages(int maxMessages) {
            this.maxMessages = maxMessages;
            return this;
        }

        public Builder keepSystemMessage(boolean keepSystemMessage) {
            this.keepSystemMessage = keepSystemMessage;
            return this;
        }

        public WindowChatMemory build() {
            MemoryOptions options = MemoryOptions.builder()
                    .maxMessages(maxMessages)
                    .keepSystemMessage(keepSystemMessage)
                    .build();

            return new WindowChatMemory(options);
        }
    }
}