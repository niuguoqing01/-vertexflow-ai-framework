package com.vertexflow.ai.core.tool;

import java.util.Map;

public record ReActScratchpadStep(
        String thought,
        String action,
        Map<String, Object> actionInput,
        String observation
) {
}