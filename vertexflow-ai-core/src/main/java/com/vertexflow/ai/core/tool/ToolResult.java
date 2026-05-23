package com.vertexflow.ai.core.tool;

public record ToolResult(
        boolean success,
        String content,
        Object rawResult,
        String errorMessage,
        String errorCode
) {
    public static ToolResult success(Object result) {
        return new ToolResult(true, String.valueOf(result), result, null, null);
    }

    public static ToolResult failure(String errorMessage) {
        return new ToolResult(false, null, null, errorMessage, "TOOL_EXECUTION_ERROR");
    }

    public static ToolResult failure(String errorCode, String errorMessage) {
        return new ToolResult(false, null, null, errorMessage, errorCode);
    }
}