package com.vertexflow.ai.memory;

import com.vertexflow.ai.core.chat.ChatMessage;
import com.vertexflow.ai.core.chat.Role;
import com.vertexflow.ai.core.memory.ChatMemory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JdbcChatMemory implements ChatMemory {

    private final String url;
    private final String username;
    private final String password;
    private final String tableName;
    private final int maxMessages;
    private final boolean autoCreateTable;

    private JdbcChatMemory(Builder builder) {
        this.url = builder.url;
        this.username = builder.username;
        this.password = builder.password;
        this.tableName = builder.tableName;
        this.maxMessages = builder.maxMessages;
        this.autoCreateTable = builder.autoCreateTable;

        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("jdbc url is required");
        }

        if (tableName == null || tableName.isBlank()) {
            throw new IllegalArgumentException("tableName is required");
        }

        if (maxMessages <= 0) {
            throw new IllegalArgumentException("maxMessages must be greater than 0");
        }

        if (autoCreateTable) {
            createTableIfNecessary();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public List<ChatMessage> get(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return List.of();
        }

        String sql = """
                SELECT role, content
                FROM %s
                WHERE conversation_id = ?
                ORDER BY id DESC
                LIMIT ?
                """.formatted(tableName);

        List<ChatMessage> messages = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, conversationId);
            statement.setInt(2, maxMessages);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String role = resultSet.getString("role");
                    String content = resultSet.getString("content");

                    messages.add(new ChatMessage(
                            Role.valueOf(role),
                            content
                    ));
                }
            }

            Collections.reverse(messages);
            return messages;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to get JDBC chat memory", e);
        }
    }

    @Override
    public void add(String conversationId, ChatMessage message) {
        if (conversationId == null || conversationId.isBlank() || message == null) {
            return;
        }

        String sql = """
                INSERT INTO %s (conversation_id, role, content, created_at)
                VALUES (?, ?, ?, ?)
                """.formatted(tableName);

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, conversationId);
            statement.setString(2, message.role().name());
            statement.setString(3, message.content());
            statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            statement.executeUpdate();

            trim(connection, conversationId);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to add JDBC chat memory", e);
        }
    }

    @Override
    public void clear(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return;
        }

        String sql = """
                DELETE FROM %s
                WHERE conversation_id = ?
                """.formatted(tableName);

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, conversationId);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to clear JDBC chat memory", e);
        }
    }

    private void createTableIfNecessary() {
        String sql = """
                CREATE TABLE IF NOT EXISTS %s (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    conversation_id VARCHAR(255) NOT NULL,
                    role VARCHAR(50) NOT NULL,
                    content CLOB NOT NULL,
                    created_at TIMESTAMP NOT NULL
                )
                """.formatted(tableName);

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(sql);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create JDBC chat memory table", e);
        }
    }

    private void trim(Connection connection, String conversationId) throws SQLException {
        String selectSql = """
                SELECT id
                FROM %s
                WHERE conversation_id = ?
                ORDER BY id DESC
                """.formatted(tableName);

        List<Long> ids = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
            statement.setString(1, conversationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ids.add(resultSet.getLong("id"));
                }
            }
        }

        if (ids.size() <= maxMessages) {
            return;
        }

        List<Long> deleteIds = ids.subList(maxMessages, ids.size());

        String deleteSql = """
                DELETE FROM %s
                WHERE id = ?
                """.formatted(tableName);

        try (PreparedStatement statement = connection.prepareStatement(deleteSql)) {
            for (Long id : deleteIds) {
                statement.setLong(1, id);
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private Connection getConnection() throws SQLException {
        if (username == null) {
            return DriverManager.getConnection(url);
        }

        return DriverManager.getConnection(url, username, password == null ? "" : password);
    }

    public static class Builder {

        private String url;
        private String username;
        private String password;
        private String tableName = "vertexflow_chat_memory";
        private int maxMessages = 20;
        private boolean autoCreateTable = true;

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public Builder maxMessages(int maxMessages) {
            this.maxMessages = maxMessages;
            return this;
        }

        public Builder autoCreateTable(boolean autoCreateTable) {
            this.autoCreateTable = autoCreateTable;
            return this;
        }

        public JdbcChatMemory build() {
            return new JdbcChatMemory(this);
        }
    }
}