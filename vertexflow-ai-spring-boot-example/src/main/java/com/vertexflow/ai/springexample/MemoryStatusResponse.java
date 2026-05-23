package com.vertexflow.ai.springexample;

public record MemoryStatusResponse(
        boolean enabled,
        String implementation
) {
}