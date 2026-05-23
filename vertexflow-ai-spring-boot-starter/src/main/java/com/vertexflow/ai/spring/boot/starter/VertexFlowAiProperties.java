package com.vertexflow.ai.spring.boot.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vertexflow.ai")
public class VertexFlowAiProperties {

    /**
     * Whether VertexFlow AI auto configuration is enabled.
     */
    private boolean enabled = true;

    /**
     * AI provider name. Current default is openai-compatible.
     */
    private String provider = "openai-compatible";

    /**
     * API key for the AI provider.
     */
    private String apiKey;

    /**
     * Base URL for OpenAI-compatible API.
     */
    private String baseUrl = "https://api.openai.com/v1";

    /**
     * Chat model name.
     */
    private String model = "gpt-4o-mini";

    /**
     * Sampling temperature.
     */
    private Double temperature = 0.7;

    /**
     * Maximum output tokens.
     */
    private Integer maxTokens = 2048;

    /**
     * Whether to print AI call logs to console.
     */
    private boolean consoleLog = false;

    /**
     * Memory configuration.
     */
    private Memory memory = new Memory();

    /**
     * RAG configuration.
     */
    private Rag rag = new Rag();

    /**
     * Tool calling configuration.
     */
    private Tool tool = new Tool();

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

    public Memory getMemory() {
        return memory;
    }

    public VertexFlowAiProperties setMemory(Memory memory) {
        this.memory = memory;
        return this;
    }

    public Rag getRag() {
        return rag;
    }

    public VertexFlowAiProperties setRag(Rag rag) {
        this.rag = rag;
        return this;
    }

    public Tool getTool() {
        return tool;
    }

    public VertexFlowAiProperties setTool(Tool tool) {
        this.tool = tool;
        return this;
    }

    public static class Memory {

        /**
         * Whether chat memory is enabled.
         */
        private boolean enabled = false;

        /**
         * Maximum messages kept in memory.
         */
        private int maxMessages = 10;

        /**
         * Conversation id used by default AiClient.
         */
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

        /**
         * Whether RAG engine is enabled.
         */
        private boolean enabled = false;

        /**
         * Top K documents to retrieve.
         */
        private int topK = 3;

        /**
         * Whether RAG answer should return source chunks.
         */
        private boolean returnSources = true;

        /**
         * Document chunk size.
         */
        private int chunkSize = 300;

        /**
         * Chunk overlap size.
         */
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

    public static class Tool {

        /**
         * Whether tool calling is enabled.
         */
        private boolean enabled = false;

        /**
         * Maximum agent execution steps.
         */
        private int maxSteps = 10;

        public boolean isEnabled() {
            return enabled;
        }

        public Tool setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public int getMaxSteps() {
            return maxSteps;
        }

        public Tool setMaxSteps(int maxSteps) {
            this.maxSteps = maxSteps;
            return this;
        }
    }
}