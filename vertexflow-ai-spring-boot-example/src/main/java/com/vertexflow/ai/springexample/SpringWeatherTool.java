package com.vertexflow.ai.springexample;

import com.vertexflow.ai.core.tool.AiTool;
import com.vertexflow.ai.core.tool.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class SpringWeatherTool {

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
}