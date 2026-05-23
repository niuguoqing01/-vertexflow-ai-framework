package com.vertexflow.ai.core.chat;

public record StreamChatResponse(
        String content,
        String model,
        boolean finished
) {

}
