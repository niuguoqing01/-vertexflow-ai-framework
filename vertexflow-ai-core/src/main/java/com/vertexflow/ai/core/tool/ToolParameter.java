package com.vertexflow.ai.core.tool;

public record ToolParameter(
        String name,
        String description,
        String type,
        boolean required
) {
}
