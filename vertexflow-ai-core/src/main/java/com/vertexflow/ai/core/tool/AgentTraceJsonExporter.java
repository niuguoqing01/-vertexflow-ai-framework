package com.vertexflow.ai.core.tool;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AgentTraceJsonExporter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String toJson(AgentResponse response) {
        if (response == null) {
            return "{}";
        }

        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(toMap(response));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to export agent trace json", e);
        }
    }

    public Map<String, Object> toMap(AgentResponse response) {
        Map<String, Object> root = new LinkedHashMap<>();

        root.put("success", response.success());
        root.put("failureReason", response.failureReason() == null ? null : response.failureReason().name());
        root.put("answer", response.answer());

        List<Map<String, Object>> steps = new ArrayList<>();

        if (response.steps() != null) {
            for (AgentStep step : response.steps()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("type", step.type() == null ? null : step.type().name());
                item.put("name", step.name());
                item.put("content", step.content());
                item.put("dataType", step.data() == null ? null : step.data().getClass().getSimpleName());

                steps.add(item);
            }
        }

        root.put("steps", steps);

        return root;
    }

    public static AgentTraceJsonExporter create() {
        return new AgentTraceJsonExporter();
    }
}