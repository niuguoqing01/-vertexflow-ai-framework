package com.vertexflow.ai.core.tool;

public enum AgentFailureReason {

    NONE,
    FORMAT_ERROR,
    TOOL_ERROR,
    MAX_STEPS_REACHED,
    MODEL_OUTPUT_INVALID
}