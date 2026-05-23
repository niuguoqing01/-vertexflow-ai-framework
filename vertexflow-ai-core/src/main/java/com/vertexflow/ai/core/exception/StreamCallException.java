package com.vertexflow.ai.core.exception;

public class StreamCallException extends AiException {

    public StreamCallException(String message) {
        super(AiErrorCode.STREAM_CALL_ERROR, message);
    }

    public StreamCallException(String message, Throwable cause) {
        super(AiErrorCode.STREAM_CALL_ERROR, message, cause);
    }
}
