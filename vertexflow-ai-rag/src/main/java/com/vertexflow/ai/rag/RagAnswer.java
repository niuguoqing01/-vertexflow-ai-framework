package com.vertexflow.ai.rag;

import java.util.List;

public record RagAnswer(
        String content,
        List<RagSource> sources,
        String context
) {
}
