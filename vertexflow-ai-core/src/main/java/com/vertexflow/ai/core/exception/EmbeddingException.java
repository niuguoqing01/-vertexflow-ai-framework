package com.vertexflow.ai.core.exception;

public class EmbeddingException extends AiException {

    public EmbeddingException(String message) {
        super(AiErrorCode.EMBEDDING_CALL_ERROR, message);
    }

    public EmbeddingException(String message, Throwable cause) {
        super(AiErrorCode.EMBEDDING_CALL_ERROR, message, cause);
    }
}
