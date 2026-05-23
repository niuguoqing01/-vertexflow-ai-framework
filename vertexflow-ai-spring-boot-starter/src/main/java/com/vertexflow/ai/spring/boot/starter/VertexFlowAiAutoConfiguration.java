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
import com.vertexflow.ai.rag.FixedSizeDocumentSplitter;
import com.vertexflow.ai.rag.InMemoryVectorStore;
import com.vertexflow.ai.rag.RagEngine;
import com.vertexflow.ai.rag.RagOptions;
import com.vertexflow.ai.rag.SimpleTextEmbedding;
import com.vertexflow.ai.rag.VectorStore;
import com.vertexflow.ai.core.tool.AiTool;
import com.vertexflow.ai.core.tool.AgentOptions;
import com.vertexflow.ai.core.tool.SimpleToolAgent;
import com.vertexflow.ai.core.tool.ToolRegistry;
import com.vertexflow.ai.model.openai.OpenAiToolCallParser;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import com.vertexflow.ai.rag.QdrantVectorStore;

import java.lang.reflect.Method;

@AutoConfiguration
@ConditionalOnClass(AiClient.class)
@EnableConfigurationProperties(VertexFlowAiProperties.class)
@ConditionalOnProperty(prefix = "vertexflow.ai", name = "enabled", havingValue = "true", matchIfMissing = true)
public class VertexFlowAiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "vertexflow.ai.rag", name = "enabled", havingValue = "true")
    public RagDocumentAutoLoader ragDocumentAutoLoader(
            RagEngine ragEngine,
            VertexFlowAiProperties properties
    ) {
        return new RagDocumentAutoLoader(ragEngine, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "vertexflow.ai.tool", name = "enabled", havingValue = "true")
    public ToolRegistry toolRegistry() {
        return new ToolRegistry();
    }

    @Bean
    @ConditionalOnProperty(prefix = "vertexflow.ai.tool", name = "enabled", havingValue = "true")
    public AiToolBeanPostProcessor aiToolBeanPostProcessor(ToolRegistry toolRegistry) {
        return new AiToolBeanPostProcessor(toolRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "vertexflow.ai.tool", name = "enabled", havingValue = "true")
    public SimpleToolAgent simpleToolAgent(
            ChatModel chatModel,
            ToolRegistry toolRegistry,
            VertexFlowAiProperties properties
    ) {
        return SimpleToolAgent.builder()
                .chatModel(chatModel)
                .toolRegistry(toolRegistry)
                .toolCallParser(new OpenAiToolCallParser())
                .options(AgentOptions.builder()
                        .maxSteps(properties.getTool().getMaxSteps())
                        .returnSteps(true)
                        .build())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "vertexflow.ai.rag", name = "enabled", havingValue = "true")
    public VectorStore vectorStore(VertexFlowAiProperties properties) {
        SimpleTextEmbedding embeddingModel = new SimpleTextEmbedding(
                properties.getVectorStore().getQdrant().getVectorSize()
        );

        String type = properties.getVectorStore().getType();

        if ("qdrant".equalsIgnoreCase(type)) {
            return QdrantVectorStore.builder()
                    .url(properties.getVectorStore().getQdrant().getUrl())
                    .collectionName(properties.getVectorStore().getQdrant().getCollectionName())
                    .vectorSize(properties.getVectorStore().getQdrant().getVectorSize())
                    .embeddingModel(embeddingModel)
                    .build();
        }

        return new InMemoryVectorStore(embeddingModel);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "vertexflow.ai.rag", name = "enabled", havingValue = "true")
    public RagEngine ragEngine(
            ChatModel chatModel,
            VectorStore vectorStore,
            VertexFlowAiProperties properties
    ) {
        RagOptions options = RagOptions.defaults()
                .setTopK(properties.getRag().getTopK())
                .setReturnSources(properties.getRag().isReturnSources());

        return new RagEngine(
                chatModel,
                vectorStore,
                new FixedSizeDocumentSplitter(
                        properties.getRag().getChunkSize(),
                        properties.getRag().getOverlap()
                ),
                options
        );
    }

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
                .system("你是 VertexFlow AI Framework 的中文 AI 助手，请优先使用中文回答。");

        ChatMemory chatMemory = chatMemoryProvider.getIfAvailable();

        if (chatMemory != null && properties.getMemory().isEnabled()) {
            builder.memory(chatMemory)
                    .conversationId(properties.getMemory().getConversationId());
        }

        return builder.build();
    }
}