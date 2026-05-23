package com.vertexflow.ai.core.exception;

public class AgentException extends AiException {

    public AgentException(AiErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public AgentException(AiErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public static AgentException maxStepsExceeded(int maxSteps) {
        return new AgentException(
                AiErrorCode.AGENT_MAX_STEPS_EXCEEDED,
                "Agent max steps exceeded. maxSteps=" + maxSteps
        );
    }

    public static AgentException modelNotSupportTool() {
        return new AgentException(
                AiErrorCode.AGENT_MODEL_NOT_SUPPORT_TOOL,
                "Current chatModel does not support tool calling"
        );
    }

    public static AgentException toolParseError(String message, Throwable cause) {
        return new AgentException(
                AiErrorCode.AGENT_TOOL_PARSE_ERROR,
                message,
                cause
        );
    }

    public static AgentException toolCallError(String message, Throwable cause) {
        return new AgentException(
                AiErrorCode.AGENT_TOOL_CALL_ERROR,
                message,
                cause
        );
    }
}