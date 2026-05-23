package com.vertexflow.ai.core;

import com.vertexflow.ai.core.chat.AiClient;
import com.vertexflow.ai.core.chat.ChatModel;

public class VertexFlowAi {

    private VertexFlowAi() {
    }

    public static AiClient client(ChatModel chatModel) {
        return AiClient.create(chatModel);
    }

    public static AiClient.Builder clientBuilder() {
        return AiClient.builder();
    }
}
