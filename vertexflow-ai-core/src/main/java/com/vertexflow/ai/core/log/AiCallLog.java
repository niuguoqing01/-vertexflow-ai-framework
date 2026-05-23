package com.vertexflow.ai.core.log;

public class AiCallLog {

    private AiCallType type;
    private String provider;
    private String model;
    private boolean success;
    private long durationMs;
    private Integer inputTokens;
    private Integer outputTokens;
    private Integer totalTokens;
    private String errorCode;
    private String errorMessage;

    private AiCallLog() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public AiCallType getType() {
        return type;
    }

    public String getProvider() {
        return provider;
    }

    public String getModel() {
        return model;
    }

    public boolean isSuccess() {
        return success;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public Integer getInputTokens() {
        return inputTokens;
    }

    public Integer getOutputTokens() {
        return outputTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static class Builder {

        private final AiCallLog log = new AiCallLog();

        public Builder type(AiCallType type) {
            log.type = type;
            return this;
        }

        public Builder provider(String provider) {
            log.provider = provider;
            return this;
        }

        public Builder model(String model) {
            log.model = model;
            return this;
        }

        public Builder success(boolean success) {
            log.success = success;
            return this;
        }

        public Builder durationMs(long durationMs) {
            log.durationMs = durationMs;
            return this;
        }

        public Builder inputTokens(Integer inputTokens) {
            log.inputTokens = inputTokens;
            return this;
        }

        public Builder outputTokens(Integer outputTokens) {
            log.outputTokens = outputTokens;
            return this;
        }

        public Builder totalTokens(Integer totalTokens) {
            log.totalTokens = totalTokens;
            return this;
        }

        public Builder errorCode(String errorCode) {
            log.errorCode = errorCode;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            log.errorMessage = errorMessage;
            return this;
        }

        public AiCallLog build() {
            return log;
        }
    }
}
