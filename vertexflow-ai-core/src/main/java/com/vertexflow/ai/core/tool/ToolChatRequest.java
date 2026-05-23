package com.vertexflow.ai.core.tool;

import com.vertexflow.ai.core.chat.ChatRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ToolChatRequest {

    private ChatRequest chatRequest;
    private List<Map<String, Object>> tools = new ArrayList<>();
    private String toolChoice = "auto";

    private ToolChatRequest() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public ChatRequest getChatRequest() {
        return chatRequest;
    }

    public List<Map<String, Object>> getTools() {
        return tools;
    }

    public String getToolChoice() {
        return toolChoice;
    }

    public static class Builder {

        private final ToolChatRequest request = new ToolChatRequest();

        public Builder chatRequest(ChatRequest chatRequest) {
            request.chatRequest = chatRequest;
            return this;
        }

        public Builder tools(List<Map<String, Object>> tools) {
            request.tools = tools == null ? new ArrayList<>() : tools;
            return this;
        }

        public Builder toolChoice(String toolChoice) {
            request.toolChoice = toolChoice;
            return this;
        }

        public ToolChatRequest build() {
            if (request.chatRequest == null) {
                throw new IllegalArgumentException("chatRequest is required");
            }

            return request;
        }
    }
}