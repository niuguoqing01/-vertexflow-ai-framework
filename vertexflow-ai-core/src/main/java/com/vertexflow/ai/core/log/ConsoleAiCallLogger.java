package com.vertexflow.ai.core.log;

public class ConsoleAiCallLogger implements AiCallLogger {

    @Override
    public void log(AiCallLog log) {
        System.out.println("[VertexFlow AI Call Log]"
                + " type=" + log.getType()
                + ", provider=" + log.getProvider()
                + ", model=" + log.getModel()
                + ", success=" + log.isSuccess()
                + ", durationMs=" + log.getDurationMs()
                + ", inputTokens=" + log.getInputTokens()
                + ", outputTokens=" + log.getOutputTokens()
                + ", totalTokens=" + log.getTotalTokens()
                + ", errorCode=" + log.getErrorCode()
                + ", errorMessage=" + log.getErrorMessage());
    }
}
