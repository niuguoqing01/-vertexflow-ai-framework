package com.vertexflow.ai.core.tool;

import java.util.List;

public record AgentResponse(
        String answer,
        List<AgentStep> steps,
        boolean success,
        AgentFailureReason failureReason
) {
    public AgentResponse(String answer, List<AgentStep> steps) {
        this(answer, steps, true, AgentFailureReason.NONE);
    }

    public static AgentResponse success(String answer, List<AgentStep> steps) {
        return new AgentResponse(answer, steps, true, AgentFailureReason.NONE);
    }

    public static AgentResponse failure(String answer, List<AgentStep> steps, AgentFailureReason reason) {
        return new AgentResponse(answer, steps, false, reason);
    }
}