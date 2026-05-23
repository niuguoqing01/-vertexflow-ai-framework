package com.vertexflow.ai.core.tool;

import java.util.List;

public record AgentResponse(
        String answer,
        List<AgentStep> steps
) {
}