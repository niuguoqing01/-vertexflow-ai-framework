package com.vertexflow.ai.core.log;

public class NoOpAiCallLogger implements AiCallLogger {

    @Override
    public void log(AiCallLog log) {
        // no operation
    }
}
