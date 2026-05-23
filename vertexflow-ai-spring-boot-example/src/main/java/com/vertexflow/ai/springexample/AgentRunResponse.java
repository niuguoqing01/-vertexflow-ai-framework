package com.vertexflow.ai.springexample;

import java.util.List;

public record AgentRunResponse(
        boolean enabled,
        boolean success,
        String failureReason,
        String answer,
        int stepCount,
        List<AgentStepResponse> steps
) {
}