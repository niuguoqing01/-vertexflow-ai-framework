package com.vertexflow.ai.core.tool;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReActScratchpad {

    private final List<ReActScratchpadStep> steps = new ArrayList<>();
    private final List<String> rawMessages = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void addRawMessage(String message) {
        if (message != null && !message.isBlank()) {
            rawMessages.add(message);
        }
    }

    public void addStep(String thought, String action, Map<String, Object> actionInput, String observation) {
        steps.add(new ReActScratchpadStep(
                thought,
                action,
                actionInput == null ? Map.of() : actionInput,
                observation
        ));
    }

    public List<ReActScratchpadStep> steps() {
        return List.copyOf(steps);
    }

    public List<String> rawMessages() {
        return List.copyOf(rawMessages);
    }

    public String render() {
        StringBuilder builder = new StringBuilder();

        for (String rawMessage : rawMessages) {
            builder.append(rawMessage).append("\n");
        }

        for (ReActScratchpadStep step : steps) {
            if (step.thought() != null && !step.thought().isBlank()) {
                builder.append("Thought: ").append(step.thought()).append("\n");
            }

            if (step.action() != null && !step.action().isBlank()) {
                builder.append("Action: ").append(step.action()).append("\n");
            }

            if (step.actionInput() != null && !step.actionInput().isEmpty()) {
                builder.append("Action Input: ")
                        .append(toJson(step.actionInput()))
                        .append("\n");
            }

            if (step.observation() != null && !step.observation().isBlank()) {
                builder.append("Observation: ").append(step.observation()).append("\n");
            }

            builder.append("\n");
        }

        return builder.toString();
    }

    private String toJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }
}