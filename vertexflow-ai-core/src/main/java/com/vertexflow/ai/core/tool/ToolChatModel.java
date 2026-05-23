package com.vertexflow.ai.core.tool;

import com.vertexflow.ai.core.chat.ChatResponse;

public interface ToolChatModel {

    ChatResponse callWithTools(ToolChatRequest request);
}