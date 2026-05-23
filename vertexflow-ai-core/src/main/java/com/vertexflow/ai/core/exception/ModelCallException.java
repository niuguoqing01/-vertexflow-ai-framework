package com.vertexflow.ai.core.exception;

public class ModelCallException extends AiException {

    public ModelCallException(String message) {
        super(AiErrorCode.MODEL_CALL_ERROR, message);
    }

    public ModelCallException(String message, Throwable cause) {
        super(AiErrorCode.MODEL_CALL_ERROR, message, cause);
    }
}
