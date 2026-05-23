package com.vertexflow.ai.core.chat;

import java.util.ArrayList;
import java.util.List;

public class ChatRequest {

    private ChatOptions options = ChatOptions.defaults();
    private List<ChatMessage> messages = new ArrayList<>();

    public ChatOptions getOptions() {
        return options;
    }

    public ChatRequest setOptions(ChatOptions options) {
        this.options = options == null ? ChatOptions.defaults() : options;
        return this;
    }

    public String getModel() {
        return options.getModel();
    }

    public ChatRequest setModel(String model) {
        this.options = ChatOptions.builder()
                .model(model)
                .temperature(options.getTemperature())
                .maxTokens(options.getMaxTokens())
                .topP(options.getTopP())
                .presencePenalty(options.getPresencePenalty())
                .frequencyPenalty(options.getFrequencyPenalty())
                .build();
        return this;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public ChatRequest setMessages(List<ChatMessage> messages) {
        this.messages = messages == null ? new ArrayList<>() : messages;
        return this;
    }

    public ChatRequest addMessage(ChatMessage message) {
        this.messages.add(message);
        return this;
    }

    public Double getTemperature() {
        return options.getTemperature();
    }

    public ChatRequest setTemperature(Double temperature) {
        this.options = ChatOptions.builder()
                .model(options.getModel())
                .temperature(temperature)
                .maxTokens(options.getMaxTokens())
                .topP(options.getTopP())
                .presencePenalty(options.getPresencePenalty())
                .frequencyPenalty(options.getFrequencyPenalty())
                .build();
        return this;
    }

    public Integer getMaxTokens() {
        return options.getMaxTokens();
    }

    public ChatRequest setMaxTokens(Integer maxTokens) {
        this.options = ChatOptions.builder()
                .model(options.getModel())
                .temperature(options.getTemperature())
                .maxTokens(maxTokens)
                .topP(options.getTopP())
                .presencePenalty(options.getPresencePenalty())
                .frequencyPenalty(options.getFrequencyPenalty())
                .build();
        return this;
    }
}