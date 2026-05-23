package com.vertexflow.ai.spring.boot.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vertexflow.ai")
public class VertexFlowAiProperties {

    private boolean enabled = true;
    private String provider = "openai-compatible";
    private String apiKey;
    private String baseUrl = "https://api.openai.com/v1";
    private String model = "gpt-4o-mini";
    private Double temperature = 0.7;
    private Integer maxTokens = 2048;
    private boolean consoleLog = false;

    public boolean isEnabled() {
        return enabled;
    }

    public VertexFlowAiProperties setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getProvider() {
        return provider;
    }

    public VertexFlowAiProperties setProvider(String provider) {
        this.provider = provider;
        return this;
    }

    public String getApiKey() {
        return apiKey;
    }

    public VertexFlowAiProperties setApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public VertexFlowAiProperties setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public String getModel() {
        return model;
    }

    public VertexFlowAiProperties setModel(String model) {
        this.model = model;
        return this;
    }

    public Double getTemperature() {
        return temperature;
    }

    public VertexFlowAiProperties setTemperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public VertexFlowAiProperties setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    public boolean isConsoleLog() {
        return consoleLog;
    }

    public VertexFlowAiProperties setConsoleLog(boolean consoleLog) {
        this.consoleLog = consoleLog;
        return this;
    }
}