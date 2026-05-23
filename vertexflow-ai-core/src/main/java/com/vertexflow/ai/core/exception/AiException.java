package com.vertexflow.ai.core.exception;

public class AiException extends RuntimeException {

    private final AiErrorCode errorCode;

    public AiException(AiErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AiException(AiErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public AiErrorCode getErrorCode() {
        return errorCode;
    }

    public String getCode() {
        return errorCode.code();
    }
}
