package com.vertexflow.ai.core.tool;

public enum AgentFailureReason {

    NONE,
    FORMAT_ERROR,
    TOOL_ERROR,
    TOOL_NOT_FOUND,
    MAX_STEPS_REACHED,
    MODEL_OUTPUT_INVALID
}