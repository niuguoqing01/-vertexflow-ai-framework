package com.vertexflow.ai.core.prompt;

import java.util.Map;
import java.util.regex.Matcher;

public class PromptTemplate {

    private final String template;

    private PromptTemplate(String template) {
        this.template = template;
    }

    public static PromptTemplate from(String template) {
        return new PromptTemplate(template);
    }

    public String render(Map<String, Object> variables) {
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String key = "\\{" + entry.getKey() + "\\}";
            String value = Matcher.quoteReplacement(String.valueOf(entry.getValue()));
            result = result.replaceAll(key, value);
        }
        return result;
    }
}
