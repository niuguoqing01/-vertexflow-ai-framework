package com.vertexflow.ai.core.tool;

public enum AgentStepType {

    USER_INPUT,
    MODEL_RESPONSE,
    TOOL_CALL,
    TOOL_RESULT,
    FORMAT_ERROR,
    TOOL_ERROR,
    MAX_STEPS,
    FINAL_ANSWER
}