package com.vertexflow.ai.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vertexflow.ai.core.embedding.EmbeddingModel;
import com.vertexflow.ai.core.embedding.EmbeddingResponse;
import com.vertexflow.ai.core.exception.AiErrorCode;
import com.vertexflow.ai.core.exception.AiException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class QdrantVectorStore implements VectorStore {

    private final String url;
    private final String collectionName;
    private final int vectorSize;
    private final EmbeddingModel embeddingModel;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Override
    public boolean exists(String chunkId) {
        if (chunkId == null || chunkId.isBlank()) {
            return false;
        }

        try {
            String pointId = toPointId(chunkId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url + "/collections/" + collectionName + "/points/" + pointId))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int deleteByDocumentId(String documentId) {
        if (documentId == null || documentId.isBlank()) {
            return 0;
        }

        Map<String, Object> match = new LinkedHashMap<>();
        match.put("value", documentId);

        Map<String, Object> condition = new LinkedHashMap<>();
        condition.put("key", "documentId");
        condition.put("match", match);

        Map<String, Object> filter = new LinkedHashMap<>();
        filter.put("must", List.of(condition));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("filter", filter);

        sendPost("/collections/" + collectionName + "/points/delete?wait=true", body);

        return -1;
    }

    private QdrantVectorStore(Builder builder) {
        this.url = removeTrailingSlash(builder.url);
        this.collectionName = builder.collectionName;
        this.vectorSize = builder.vectorSize;
        this.embeddingModel = builder.embeddingModel;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();

        if (this.url == null || this.url.isBlank()) {
            throw new IllegalArgumentException("url is required");
        }

        if (this.collectionName == null || this.collectionName.isBlank()) {
            throw new IllegalArgumentException("collectionName is required");
        }

        if (this.vectorSize <= 0) {
            throw new IllegalArgumentException("vectorSize must be greater than 0");
        }

        if (this.embeddingModel == null) {
            throw new IllegalArgumentException("embeddingModel is required");
        }

        createCollectionIfNotExists();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void add(List<DocumentChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return;
        }

        List<Map<String, Object>> points = new ArrayList<>();

        for (DocumentChunk chunk : chunks) {
            if (chunk == null || chunk.content() == null || chunk.content().isBlank()) {
                continue;
            }

            EmbeddingResponse embeddingResponse = embeddingModel.embed(chunk.content());
            double[] vector = embeddingResponse.vector();

            if (vector == null || vector.length == 0) {
                throw new AiException(AiErrorCode.VECTOR_STORE_ERROR, "Embedding vector is empty");
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("chunkId", chunk.id());
            payload.put("documentId", chunk.documentId());
            payload.put("content", chunk.content());

            Map<String, Object> point = new LinkedHashMap<>();
            point.put("id", toPointId(chunk.id()));
            point.put("vector", toList(vector));
            point.put("payload", payload);

            points.add(point);
        }

        if (points.isEmpty()) {
            return;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("points", points);

        sendPut("/collections/" + collectionName + "/points?wait=true", body);
    }

    @Override
    public List<VectorSearchResult> search(String query, int topK) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        EmbeddingResponse embeddingResponse = embeddingModel.embed(query);
        double[] vector = embeddingResponse.vector();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("vector", toList(vector));
        body.put("limit", topK);
        body.put("with_payload", true);

        JsonNode root = sendPost("/collections/" + collectionName + "/points/search", body);

        JsonNode resultNode = root.path("result");

        if (!resultNode.isArray()) {
            return List.of();
        }

        List<VectorSearchResult> results = new ArrayList<>();

        for (JsonNode item : resultNode) {
            double score = item.path("score").asDouble();

            JsonNode payload = item.path("payload");

            String chunkId = payload.path("chunkId").asText();
            String documentId = payload.path("documentId").asText();
            String content = payload.path("content").asText();

            DocumentChunk chunk = new DocumentChunk(chunkId, documentId, content);

            results.add(new VectorSearchResult(chunk, score));
        }

        return results;
    }

    private void createCollectionIfNotExists() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url + "/collections/" + collectionName))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return;
            }

            Map<String, Object> vectors = new LinkedHashMap<>();
            vectors.put("size", vectorSize);
            vectors.put("distance", "Cosine");

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("vectors", vectors);

            sendPut("/collections/" + collectionName, body);
        } catch (Exception e) {
            throw new AiException(AiErrorCode.VECTOR_STORE_ERROR, "Failed to create Qdrant collection", e);
        }
    }

    private JsonNode sendPost(String path, Object body) {
        return send("POST", path, body);
    }

    private JsonNode sendPut(String path, Object body) {
        return send("PUT", path, body);
    }

    private JsonNode send(String method, String path, Object body) {
        try {
            String json = objectMapper.writeValueAsString(body);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url + path))
                    .header("Content-Type", "application/json");

            if ("POST".equalsIgnoreCase(method)) {
                builder.POST(HttpRequest.BodyPublishers.ofString(json));
            } else if ("PUT".equalsIgnoreCase(method)) {
                builder.PUT(HttpRequest.BodyPublishers.ofString(json));
            } else {
                throw new IllegalArgumentException("Unsupported method: " + method);
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new AiException(
                        AiErrorCode.VECTOR_STORE_ERROR,
                        "Qdrant request failed. status=" + response.statusCode() + ", body=" + response.body()
                );
            }

            return objectMapper.readTree(response.body());
        } catch (AiException e) {
            throw e;
        } catch (Exception e) {
            throw new AiException(AiErrorCode.VECTOR_STORE_ERROR, "Qdrant request error", e);
        }
    }

    private List<Double> toList(double[] vector) {
        List<Double> list = new ArrayList<>(vector.length);

        for (double value : vector) {
            list.add(value);
        }

        return list;
    }

    private String toPointId(String id) {
        String value = id == null || id.isBlank()
                ? String.valueOf(System.nanoTime())
                : id;

        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8)).toString();
    }

    private static String removeTrailingSlash(String value) {
        if (value == null) {
            return null;
        }

        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }

        return value;
    }

    public static class Builder {

        private String url = "http://localhost:6333";
        private String collectionName = "vertexflow_docs";
        private int vectorSize = 256;
        private EmbeddingModel embeddingModel;

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder collectionName(String collectionName) {
            this.collectionName = collectionName;
            return this;
        }

        public Builder vectorSize(int vectorSize) {
            this.vectorSize = vectorSize;
            return this;
        }

        public Builder embeddingModel(EmbeddingModel embeddingModel) {
            this.embeddingModel = embeddingModel;
            return this;
        }

        public QdrantVectorStore build() {
            return new QdrantVectorStore(this);
        }
    }
}