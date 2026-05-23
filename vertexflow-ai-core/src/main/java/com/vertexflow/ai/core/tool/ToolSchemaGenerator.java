package com.vertexflow.ai.core.tool;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ToolSchemaGenerator {

    private ToolSchemaGenerator() {
    }

    public static Map<String, Object> toSchema(ToolDefinition definition) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("name", definition.name());
        schema.put("description", definition.description());

        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();

        List<String> required = definition.parameters()
                .stream()
                .filter(ToolParameter::required)
                .map(ToolParameter::name)
                .toList();

        for (ToolParameter parameter : definition.parameters()) {
            Map<String, Object> property = new LinkedHashMap<>();
            property.put("type", parameter.type());
            property.put("description", parameter.description());

            properties.put(parameter.name(), property);
        }

        parameters.put("properties", properties);
        parameters.put("required", required);

        schema.put("parameters", parameters);

        return schema;
    }

    public static Map<String, Object> toOpenAiToolSchema(ToolDefinition definition) {
        Map<String, Object> tool = new LinkedHashMap<>();
        tool.put("type", "function");

        Map<String, Object> function = new LinkedHashMap<>();
        function.put("name", definition.name());
        function.put("description", definition.description());

        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("type", "object");

        Map<String, Object> properties = new LinkedHashMap<>();

        List<String> required = definition.parameters()
                .stream()
                .filter(ToolParameter::required)
                .map(ToolParameter::name)
                .toList();

        for (ToolParameter parameter : definition.parameters()) {
            Map<String, Object> property = new LinkedHashMap<>();
            property.put("type", parameter.type());
            property.put("description", parameter.description());

            properties.put(parameter.name(), property);
        }

        parameters.put("properties", properties);
        parameters.put("required", required);

        function.put("parameters", parameters);
        tool.put("function", function);

        return tool;
    }
}
