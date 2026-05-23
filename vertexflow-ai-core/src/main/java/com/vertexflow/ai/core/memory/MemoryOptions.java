package com.vertexflow.ai.core.memory;

public class MemoryOptions {

    private int maxMessages = 10;
    private boolean keepSystemMessage = false;

    private MemoryOptions() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static MemoryOptions defaults() {
        return builder().build();
    }

    public int getMaxMessages() {
        return maxMessages;
    }

    public boolean isKeepSystemMessage() {
        return keepSystemMessage;
    }

    public static class Builder {

        private final MemoryOptions options = new MemoryOptions();

        public Builder maxMessages(int maxMessages) {
            if (maxMessages <= 0) {
                throw new IllegalArgumentException("maxMessages must be greater than 0");
            }

            options.maxMessages = maxMessages;
            return this;
        }

        public Builder keepSystemMessage(boolean keepSystemMessage) {
            options.keepSystemMessage = keepSystemMessage;
            return this;
        }

        public MemoryOptions build() {
            return options;
        }
    }
}
