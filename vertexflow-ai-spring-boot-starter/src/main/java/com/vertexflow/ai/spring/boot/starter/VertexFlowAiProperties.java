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
    private Memory memory = new Memory();
    private Rag rag = new Rag();

    public Rag getRag() {
        return rag;
    }

    public VertexFlowAiProperties setRag(Rag rag) {
        this.rag = rag;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Memory getMemory() {
        return memory;
    }

    public VertexFlowAiProperties setMemory(Memory memory) {
        this.memory = memory;
        return this;
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

    public static class Memory {

        private boolean enabled = false;
        private int maxMessages = 10;
        private String conversationId = "default";

        public boolean isEnabled() {
            return enabled;
        }

        public Memory setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public int getMaxMessages() {
            return maxMessages;
        }

        public Memory setMaxMessages(int maxMessages) {
            this.maxMessages = maxMessages;
            return this;
        }

        public String getConversationId() {
            return conversationId;
        }

        public Memory setConversationId(String conversationId) {
            this.conversationId = conversationId;
            return this;
        }
    }

    public static class Rag {

        private boolean enabled = false;
        private int topK = 3;
        private boolean returnSources = true;
        private int chunkSize = 300;
        private int overlap = 50;

        public boolean isEnabled() {
            return enabled;
        }

        public Rag setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public int getTopK() {
            return topK;
        }

        public Rag setTopK(int topK) {
            this.topK = topK;
            return this;
        }

        public boolean isReturnSources() {
            return returnSources;
        }

        public Rag setReturnSources(boolean returnSources) {
            this.returnSources = returnSources;
            return this;
        }

        public int getChunkSize() {
            return chunkSize;
        }

        public Rag setChunkSize(int chunkSize) {
            this.chunkSize = chunkSize;
            return this;
        }

        public int getOverlap() {
            return overlap;
        }

        public Rag setOverlap(int overlap) {
            this.overlap = overlap;
            return this;
        }
    }
}