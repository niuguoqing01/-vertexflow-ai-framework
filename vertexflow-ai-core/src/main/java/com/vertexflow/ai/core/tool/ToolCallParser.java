package com.vertexflow.ai.core.tool;

import java.util.List;

public interface ToolCallParser {

    List<ToolCall> parse(String rawResponse);
}
