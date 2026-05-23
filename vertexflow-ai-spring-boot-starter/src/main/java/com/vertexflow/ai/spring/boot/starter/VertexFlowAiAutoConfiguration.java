package com.vertexflow.ai.spring.boot.starter;

import com.vertexflow.ai.core.chat.AiClient;
import com.vertexflow.ai.core.chat.ChatModel;
import com.vertexflow.ai.core.log.AiCallLogger;
import com.vertexflow.ai.core.log.ConsoleAiCallLogger;
import com.vertexflow.ai.core.log.NoOpAiCallLogger;
import com.vertexflow.ai.core.memory.ChatMemory;
import com.vertexflow.ai.memory.WindowChatMemory;
import com.vertexflow.ai.model.openai.OpenAiCompatibleChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(AiClient.class)
@EnableConfigurationProperties(VertexFlowAiProperties.class)
@ConditionalOnProperty(prefix = "vertexflow.ai", name = "enabled", havingValue = "true", matchIfMissing = true)
public class VertexFlowAiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AiCallLogger aiCallLogger(VertexFlowAiProperties properties) {
        return properties.isConsoleLog()
                ? new ConsoleAiCallLogger()
                : new NoOpAiCallLogger();
    }

    @Bean
    @ConditionalOnMissingBean
    public ChatModel chatModel(VertexFlowAiProperties properties, AiCallLogger aiCallLogger) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new IllegalArgumentException("vertexflow.ai.api-key is required");
        }

        return OpenAiCompatibleChatModel.builder()
                .apiKey(properties.getApiKey())
                .baseUrl(properties.getBaseUrl())
                .model(properties.getModel())
                .callLogger(aiCallLogger)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "vertexflow.ai.memory", name = "enabled", havingValue = "true")
    public ChatMemory chatMemory(VertexFlowAiProperties properties) {
        return WindowChatMemory.builder()
                .maxMessages(properties.getMemory().getMaxMessages())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public AiClient aiClient(
            ChatModel chatModel,
            VertexFlowAiProperties properties,
            ObjectProvider<ChatMemory> chatMemoryProvider
    ) {
        AiClient.Builder builder = AiClient.builder()
                .chatModel(chatModel)
                .system("You are VertexFlow AI assistant.");

        ChatMemory chatMemory = chatMemoryProvider.getIfAvailable();

        if (chatMemory != null && properties.getMemory().isEnabled()) {
            builder.memory(chatMemory)
                    .conversationId(properties.getMemory().getConversationId());
        }

        return builder.build();
    }
}