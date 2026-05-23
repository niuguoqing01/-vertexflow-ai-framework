package com.vertexflow.ai.core.tool;

public record ToolResult(
        boolean success,
        String content,
        Object rawResult,
        String errorMessage
) {
    public static ToolResult success(Object result) {
        return new ToolResult(true, String.valueOf(result), result, null);
    }

    public static ToolResult failure(String errorMessage) {
        return new ToolResult(false, null, null, errorMessage);
    }
}
