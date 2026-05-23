package com.vertexflow.ai.rag;

import com.vertexflow.ai.core.exception.AiErrorCode;
import com.vertexflow.ai.core.exception.AiException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class UrlDocumentLoader {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private UrlDocumentLoader() {
    }

    public static Document load(String url) {
        if (url == null || url.isBlank()) {
            throw new AiException(AiErrorCode.INVALID_REQUEST, "URL must not be blank");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("User-Agent", "VertexFlow-AI-Framework")
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new AiException(
                        AiErrorCode.DOCUMENT_LOAD_ERROR,
                        "Failed to load URL. status=" + response.statusCode() + ", url=" + url
                );
            }

            String body = response.body();

            if (body == null || body.isBlank()) {
                throw new AiException(
                        AiErrorCode.DOCUMENT_LOAD_ERROR,
                        "URL content is empty: " + url
                );
            }

            String text = cleanHtml(body);

            if (text.isBlank()) {
                throw new AiException(
                        AiErrorCode.DOCUMENT_LOAD_ERROR,
                        "URL text content is empty: " + url
                );
            }

            return new Document(url, text);
        } catch (AiException e) {
            throw e;
        } catch (Exception e) {
            throw new AiException(
                    AiErrorCode.DOCUMENT_LOAD_ERROR,
                    "Failed to load URL document: " + url,
                    e
            );
        }
    }

    private static String cleanHtml(String html) {
        return html
                .replaceAll("(?is)<script.*?>.*?</script>", " ")
                .replaceAll("(?is)<style.*?>.*?</style>", " ")
                .replaceAll("(?is)<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replaceAll("\\s+", " ")
                .trim();
    }
}