package com.vertexflow.ai.examples;

import com.vertexflow.ai.core.tool.AiTool;
import com.vertexflow.ai.core.tool.ToolParam;

public class DemoWeatherTool {

    @AiTool(name = "getWeather", description = "Get weather by city name")
    public String getWeather(
            @ToolParam(value = "city", description = "City name") String city
    ) {
        return city + " is sunny today.";
    }

    @AiTool(name = "calculateSum", description = "Calculate sum of two integers")
    public Integer calculateSum(
            @ToolParam(value = "a", description = "First number") Integer a,
            @ToolParam(value = "b", description = "Second number") Integer b
    ) {
        return a + b;
    }
    @AiTool(name = "failTool", description = "A demo tool that always fails")
    public String failTool(
            @ToolParam(value = "reason", description = "Failure reason") String reason
    ) {
        throw new IllegalStateException("Demo tool failed: " + reason);
    }
}
