package com.vertexflow.ai.core.tool;

import com.vertexflow.ai.core.exception.AiErrorCode;
import com.vertexflow.ai.core.exception.AiException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ToolRegistry {

    private final Map<String, ToolDefinition> tools = new LinkedHashMap<>();

    public void register(Object toolObject) {
        if (toolObject == null) {
            throw new AiException(AiErrorCode.INVALID_REQUEST, "toolObject must not be null");
        }

        Class<?> clazz = toolObject.getClass();

        for (Method method : clazz.getDeclaredMethods()) {
            AiTool aiTool = method.getAnnotation(AiTool.class);

            if (aiTool == null) {
                continue;
            }

            method.setAccessible(true);

            String toolName = aiTool.name();

            if (tools.containsKey(toolName)) {
                throw new AiException(AiErrorCode.INVALID_REQUEST, "Duplicate tool name: " + toolName);
            }

            ToolDefinition definition = new ToolDefinition(
                    toolName,
                    aiTool.description(),
                    toolObject,
                    method,
                    parseParameters(method)
            );

            tools.put(toolName, definition);
        }
    }

    public ToolDefinition get(String name) {
        ToolDefinition definition = tools.get(name);

        if (definition == null) {
            throw new AiException(AiErrorCode.INVALID_REQUEST, "Tool not found: " + name);
        }

        return definition;
    }

    public List<ToolDefinition> list() {
        return new ArrayList<>(tools.values());
    }

    public ToolResult execute(String name, Map<String, Object> arguments) {
        try {
            ToolDefinition definition = get(name);
            Method method = definition.method();

            Object[] args = buildMethodArguments(method, arguments == null ? Map.of() : arguments);
            Object result = method.invoke(definition.target(), args);

            return ToolResult.success(result);
        } catch (AiException e) {
            throw e;
        } catch (Exception e) {
            return ToolResult.failure("Tool execution failed: " + e.getMessage());
        }
    }

    private List<ToolParameter> parseParameters(Method method) {
        List<ToolParameter> parameters = new ArrayList<>();

        for (Parameter parameter : method.getParameters()) {
            ToolParam toolParam = parameter.getAnnotation(ToolParam.class);

            String name = toolParam == null ? parameter.getName() : toolParam.value();
            String description = toolParam == null ? "" : toolParam.description();
            boolean required = toolParam == null || toolParam.required();

            parameters.add(new ToolParameter(
                    name,
                    description,
                    toToolType(parameter.getType()),
                    required
            ));
        }

        return parameters;
    }

    private Object[] buildMethodArguments(Method method, Map<String, Object> arguments) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            ToolParam toolParam = parameter.getAnnotation(ToolParam.class);

            String name = toolParam == null ? parameter.getName() : toolParam.value();
            boolean required = toolParam == null || toolParam.required();

            Object value = arguments.get(name);

            if (value == null && required) {
                throw new AiException(AiErrorCode.INVALID_REQUEST, "Missing required tool parameter: " + name);
            }

            args[i] = convertValue(value, parameter.getType());
        }

        return args;
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        if (targetType.isInstance(value)) {
            return value;
        }

        if (targetType == String.class) {
            return String.valueOf(value);
        }

        if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(String.valueOf(value));
        }

        if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(String.valueOf(value));
        }

        if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(String.valueOf(value));
        }

        if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(String.valueOf(value));
        }

        return value;
    }

    private String toToolType(Class<?> type) {
        if (type == String.class) {
            return "string";
        }

        if (type == Integer.class || type == int.class || type == Long.class || type == long.class) {
            return "integer";
        }

        if (type == Double.class || type == double.class || type == Float.class || type == float.class) {
            return "number";
        }

        if (type == Boolean.class || type == boolean.class) {
            return "boolean";
        }

        return "object";
    }
}
