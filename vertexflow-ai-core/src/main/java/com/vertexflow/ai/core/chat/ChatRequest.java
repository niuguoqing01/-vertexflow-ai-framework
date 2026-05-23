package com.vertexflow.ai.core.chat;

import java.util.ArrayList;
import java.util.List;

public class ChatRequest {

    private String model;
    private List<ChatMessage> messages = new ArrayList<>();
    private Double temperature = 0.7;
    private Integer maxTokens = 2048;

    public String getModel() {
        return model;
    }

    public ChatRequest setModel(String model) {
        this.model = model;
        return this;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public ChatRequest setMessages(List<ChatMessage> messages) {
        this.messages = messages;
        return this;
    }

    public ChatRequest addMessage(ChatMessage message) {
        this.messages.add(message);
        return this;
    }

    public Double getTemperature() {
        return temperature;
    }

    public ChatRequest setTemperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public ChatRequest setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }
}
