package com.vertexflow.ai.core.tool;

import java.lang.reflect.Method;
import java.util.List;

public record ToolDefinition(
        String name,
        String description,
        Object target,
        Method method,
        List<ToolParameter> parameters
) {
}
