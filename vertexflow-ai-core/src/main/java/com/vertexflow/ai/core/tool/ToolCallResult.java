package com.vertexflow.ai.core.tool;

public record ToolCallResult(
        ToolCall toolCall,
        ToolResult result
) {
}
