package com.vertexflow.ai.memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vertexflow.ai.core.chat.ChatMessage;
import com.vertexflow.ai.core.memory.ChatMemory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;

public class RedisChatMemory implements ChatMemory {

    private final JedisPool jedisPool;
    private final String keyPrefix;
    private final int maxMessages;
    private final int ttlSeconds;
    private final int database;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private RedisChatMemory(Builder builder) {
        if (builder.password != null && !builder.password.isBlank()) {
            this.jedisPool = new JedisPool(
                    new redis.clients.jedis.JedisPoolConfig(),
                    builder.host,
                    builder.port,
                    2000,
                    builder.password
            );
        } else {
            this.jedisPool = new JedisPool(builder.host, builder.port);
        }
        this.keyPrefix = builder.keyPrefix;
        this.maxMessages = builder.maxMessages;
        this.ttlSeconds = builder.ttlSeconds;
        this.database = builder.database;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<ChatMessage> get(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return List.of();
        }

        String key = buildKey(conversationId);

        try (Jedis jedis = jedisPool.getResource()) {
            selectDatabase(jedis);

            List<String> values = jedis.lrange(key, 0, -1);
            List<ChatMessage> messages = new ArrayList<>();

            for (String value : values) {
                messages.add(objectMapper.readValue(value, ChatMessage.class));
            }

            return messages;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to get Redis chat memory", e);
        }
    }

    @Override
    public void add(String conversationId, ChatMessage message) {
        if (conversationId == null || conversationId.isBlank() || message == null) {
            return;
        }

        String key = buildKey(conversationId);

        try (Jedis jedis = jedisPool.getResource()) {
            selectDatabase(jedis);

            String json = objectMapper.writeValueAsString(message);

            jedis.rpush(key, json);
            jedis.ltrim(key, Math.max(0, -maxMessages), -1);

            if (ttlSeconds > 0) {
                jedis.expire(key, ttlSeconds);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to add Redis chat memory", e);
        }
    }

    @Override
    public void clear(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            selectDatabase(jedis);
            jedis.del(buildKey(conversationId));
        }
    }

    private void selectDatabase(Jedis jedis) {
        if (database > 0) {
            jedis.select(database);
        }
    }

    private String buildKey(String conversationId) {
        return keyPrefix + ":" + conversationId;
    }

    public static class Builder {

        private String host = "localhost";
        private int port = 6379;
        private String password;
        private int database = 0;
        private String keyPrefix = "vertexflow:chat-memory";
        private int maxMessages = 20;
        private int ttlSeconds = 0;

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder database(int database) {
            this.database = database;
            return this;
        }

        public Builder keyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
            return this;
        }

        public Builder maxMessages(int maxMessages) {
            this.maxMessages = maxMessages;
            return this;
        }

        public Builder ttlSeconds(int ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
            return this;
        }

        public RedisChatMemory build() {
            return new RedisChatMemory(this);
        }
    }
}