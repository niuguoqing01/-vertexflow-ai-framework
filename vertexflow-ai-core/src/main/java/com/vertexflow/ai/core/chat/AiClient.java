package com.vertexflow.ai.core.chat;

import java.util.ArrayList;
import java.util.List;

public class AiClient {

    private final ChatModel chatModel;
    private final List<ChatMessage> systemMessages = new ArrayList<>();

    private AiClient(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public static AiClient create(ChatModel chatModel) {
        return new AiClient(chatModel);
    }

    public AiClient system(String content) {
        this.systemMessages.add(ChatMessage.system(content));
        return this;
    }

    public String chat(String userMessage) {
        ChatRequest request = new ChatRequest();
        for (ChatMessage message : systemMessages) {
            request.addMessage(message);
        }
        request.addMessage(ChatMessage.user(userMessage));
        return chatModel.call(request).content();
    }
}
