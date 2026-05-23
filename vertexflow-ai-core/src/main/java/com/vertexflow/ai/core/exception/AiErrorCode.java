package com.vertexflow.ai.core.exception;

public enum AiErrorCode {

    INVALID_REQUEST("INVALID_REQUEST", "Invalid request"),
    MODEL_CALL_ERROR("MODEL_CALL_ERROR", "Model call error"),
    STREAM_CALL_ERROR("STREAM_CALL_ERROR", "Streaming model call error"),
    EMBEDDING_CALL_ERROR("EMBEDDING_CALL_ERROR", "Embedding call error"),
    DOCUMENT_LOAD_ERROR("DOCUMENT_LOAD_ERROR", "Document load error"),
    VECTOR_STORE_ERROR("VECTOR_STORE_ERROR", "Vector store error"),
    RAG_ERROR("RAG_ERROR", "RAG error"),
    AGENT_ERROR("AGENT_ERROR", "Agent error"),
    AGENT_MAX_STEPS_EXCEEDED("AGENT_MAX_STEPS_EXCEEDED", "Agent max steps exceeded"),
    AGENT_TOOL_CALL_ERROR("AGENT_TOOL_CALL_ERROR", "Agent tool call error"),
    AGENT_TOOL_PARSE_ERROR("AGENT_TOOL_PARSE_ERROR", "Agent tool parse error"),
    AGENT_MODEL_NOT_SUPPORT_TOOL("AGENT_MODEL_NOT_SUPPORT_TOOL", "Agent model does not support tool calling"),
    UNSUPPORTED_OPERATION("UNSUPPORTED_OPERATION", "Unsupported operation"),
    UNKNOWN_ERROR("UNKNOWN_ERROR", "Unknown error");

    private final String code;
    private final String message;

    AiErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}