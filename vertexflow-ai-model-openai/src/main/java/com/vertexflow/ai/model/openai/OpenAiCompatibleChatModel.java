package com.vertexflow.ai.model.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vertexflow.ai.core.chat.*;
import com.vertexflow.ai.core.chat.ChatStreamHandler;
import com.vertexflow.ai.core.chat.StreamChatResponse;
import com.vertexflow.ai.core.chat.StreamingChatModel;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.vertexflow.ai.core.exception.ModelCallException;
import com.vertexflow.ai.core.exception.StreamCallException;
import com.vertexflow.ai.core.exception.AiException;
import com.vertexflow.ai.core.log.AiCallLog;
import com.vertexflow.ai.core.log.AiCallLogger;
import com.vertexflow.ai.core.log.AiCallType;
import com.vertexflow.ai.core.log.NoOpAiCallLogger;

@SuppressWarnings("unchecked")
public class OpenAiCompatibleChatModel implements StreamingChatModel {

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AiCallLogger callLogger;

    private OpenAiCompatibleChatModel(Builder builder) {
        this.apiKey = builder.apiKey;
        this.baseUrl = builder.baseUrl;
        this.model = builder.model;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.callLogger = builder.callLogger == null ? new NoOpAiCallLogger() : builder.callLogger;
    }

    public static Builder builder() {
        return new Builder();

    }

    @Override
    public ChatResponse call(ChatRequest request) {
        long startTime = System.currentTimeMillis();
        String finalModel = request.getModel() == null ? model : request.getModel();
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", finalModel);
            body.put("temperature", request.getTemperature());
            body.put("max_tokens", request.getMaxTokens());

            List<Map<String, String>> messages = new ArrayList<>();
            for (ChatMessage message : request.getMessages()) {
                Map<String, String> item = new LinkedHashMap<>();
                item.put("role", message.role().name().toLowerCase());
                item.put("content", message.content());
                messages.add(item);
            }
            body.put("messages", messages);

            String json = objectMapper.writeValueAsString(body);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ModelCallException("AI request failed. status=" + response.statusCode() + ", body=" + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode choice = root.path("choices").get(0);

            String content = choice.path("message").path("content").asText();
            String finishReason = choice.path("finish_reason").isMissingNode()
                    ? null
                    : choice.path("finish_reason").asText(null);

            Integer inputTokens = root.path("usage").path("prompt_tokens").isMissingNode()
                    ? null
                    : root.path("usage").path("prompt_tokens").asInt();

            Integer outputTokens = root.path("usage").path("completion_tokens").isMissingNode()
                    ? null
                    : root.path("usage").path("completion_tokens").asInt();

            Integer totalTokens = root.path("usage").path("total_tokens").isMissingNode()
                    ? null
                    : root.path("usage").path("total_tokens").asInt();

            ChatResponse chatResponse = new ChatResponse(
                    content,
                    finalModel,
                    new com.vertexflow.ai.core.chat.TokenUsage(inputTokens, outputTokens, totalTokens),
                    finishReason,
                    response.body()
            );

            callLogger.log(AiCallLog.builder()
                    .type(AiCallType.CHAT)
                    .provider("openai-compatible")
                    .model(finalModel)
                    .success(true)
                    .durationMs(System.currentTimeMillis() - startTime)
                    .inputTokens(inputTokens)
                    .outputTokens(outputTokens)
                    .totalTokens(totalTokens)
                    .build());

            return chatResponse;
        } catch (Exception e) {
            String errorCode = e instanceof AiException aiException
                    ? aiException.getCode()
                    : "UNKNOWN_ERROR";

            callLogger.log(AiCallLog.builder()
                    .type(AiCallType.CHAT)
                    .provider("openai-compatible")
                    .model(finalModel)
                    .success(false)
                    .durationMs(System.currentTimeMillis() - startTime)
                    .errorCode(errorCode)
                    .errorMessage(e.getMessage())
                    .build());

            if (e instanceof ModelCallException modelCallException) {
                throw modelCallException;
            }

            throw new ModelCallException("OpenAI compatible chat model call error", e);
        }
    }

    public static class Builder {
        private String apiKey;
        private String baseUrl = "https://api.openai.com/v1";
        private String model = "gpt-4o-mini";
        private AiCallLogger callLogger;

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public OpenAiCompatibleChatModel build() {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalArgumentException("apiKey is required");
            }
            if (baseUrl == null || baseUrl.isBlank()) {
                throw new IllegalArgumentException("baseUrl is required");
            }
            if (model == null || model.isBlank()) {
                throw new IllegalArgumentException("model is required");
            }
            return new OpenAiCompatibleChatModel(this);
        }

        public Builder callLogger(AiCallLogger callLogger) {
            this.callLogger = callLogger;
            return this;
        }
    }
    @Override
    public void stream(ChatRequest request, ChatStreamHandler handler) {
        long startTime = System.currentTimeMillis();
        String finalModel = request.getModel() == null ? model : request.getModel();
        try {

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", finalModel);

            if (request.getOptions().getTemperature() != null) {
                body.put("temperature", request.getOptions().getTemperature());
            }

            if (request.getOptions().getMaxTokens() != null) {
                body.put("max_tokens", request.getOptions().getMaxTokens());
            }

            if (request.getOptions().getTopP() != null) {
                body.put("top_p", request.getOptions().getTopP());
            }

            if (request.getOptions().getPresencePenalty() != null) {
                body.put("presence_penalty", request.getOptions().getPresencePenalty());
            }

            if (request.getOptions().getFrequencyPenalty() != null) {
                body.put("frequency_penalty", request.getOptions().getFrequencyPenalty());
            }
            body.put("stream", true);

            List<Map<String, String>> messages = new ArrayList<>();
            for (ChatMessage message : request.getMessages()) {
                Map<String, String> item = new LinkedHashMap<>();
                item.put("role", message.role().name().toLowerCase());
                item.put("content", message.content());
                messages.add(item);
            }
            body.put("messages", messages);

            String json = objectMapper.writeValueAsString(body);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<java.io.InputStream> response = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String errorBody = new String(response.body().readAllBytes());
                throw new StreamCallException("AI stream request failed. status=" + response.statusCode() + ", body=" + errorBody);
            }

            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(response.body(), java.nio.charset.StandardCharsets.UTF_8)
            )) {
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }

                    if (!line.startsWith("data:")) {
                        continue;
                    }

                    String data = line.substring("data:".length()).trim();

                    if ("[DONE]".equals(data)) {
                        handler.onMessage(new StreamChatResponse("", finalModel, true));

                        callLogger.log(AiCallLog.builder()
                                .type(AiCallType.STREAM_CHAT)
                                .provider("openai-compatible")
                                .model(finalModel)
                                .success(true)
                                .durationMs(System.currentTimeMillis() - startTime)
                                .build());

                        break;
                    }

                    JsonNode root = objectMapper.readTree(data);
                    JsonNode delta = root.path("choices").get(0).path("delta");
                    String content = delta.path("content").asText("");

                    if (!content.isEmpty()) {
                        handler.onMessage(new StreamChatResponse(content, finalModel, false));
                    }
                }
            }
        } catch (Exception e) {
            String errorCode = e instanceof AiException aiException
                    ? aiException.getCode()
                    : "UNKNOWN_ERROR";

            callLogger.log(AiCallLog.builder()
                    .type(AiCallType.STREAM_CHAT)
                    .provider("openai-compatible")
                    .model(finalModel)
                    .success(false)
                    .durationMs(System.currentTimeMillis() - startTime)
                    .errorCode(errorCode)
                    .errorMessage(e.getMessage())
                    .build());

            if (e instanceof StreamCallException streamCallException) {
                throw streamCallException;
            }

            throw new StreamCallException("OpenAI compatible streaming chat model call error", e);
        }
    }
}
