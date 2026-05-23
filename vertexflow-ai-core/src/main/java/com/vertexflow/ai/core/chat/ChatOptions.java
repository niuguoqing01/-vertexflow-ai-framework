package com.vertexflow.ai.core.chat;

public class ChatOptions {

    private String model;
    private Double temperature = 0.7;
    private Integer maxTokens = 2048;
    private Double topP;
    private Double presencePenalty;
    private Double frequencyPenalty;

    private ChatOptions() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ChatOptions defaults() {
        return builder().build();
    }

    public String getModel() {
        return model;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public Double getTopP() {
        return topP;
    }

    public Double getPresencePenalty() {
        return presencePenalty;
    }

    public Double getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public static class Builder {

        private final ChatOptions options = new ChatOptions();

        public Builder model(String model) {
            options.model = model;
            return this;
        }

        public Builder temperature(Double temperature) {
            options.temperature = temperature;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            options.maxTokens = maxTokens;
            return this;
        }

        public Builder topP(Double topP) {
            options.topP = topP;
            return this;
        }

        public Builder presencePenalty(Double presencePenalty) {
            options.presencePenalty = presencePenalty;
            return this;
        }

        public Builder frequencyPenalty(Double frequencyPenalty) {
            options.frequencyPenalty = frequencyPenalty;
            return this;
        }

        public ChatOptions build() {
            return options;
        }
    }
}
