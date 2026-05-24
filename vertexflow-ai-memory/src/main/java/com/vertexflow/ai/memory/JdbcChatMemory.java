package com.vertexflow.ai.memory;

import com.vertexflow.ai.core.chat.ChatMessage;
import com.vertexflow.ai.core.chat.Role;
import com.vertexflow.ai.core.memory.ChatMemory;
import javax.sql.DataSource;

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
    private final DataSource dataSource;

    private JdbcChatMemory(Builder builder) {
        this.dataSource = builder.dataSource;
        this.url = builder.url;
        this.username = builder.username;
        this.password = builder.password;
        this.tableName = builder.tableName;
        this.maxMessages = builder.maxMessages;
        this.autoCreateTable = builder.autoCreateTable;

        if (dataSource == null && (url == null || url.isBlank())) {
            throw new IllegalArgumentException("jdbc url or dataSource is required");
        }

        if (tableName == null || tableName.isBlank()) {
            throw new IllegalArgumentException("tableName is required");
        }
        validateTableName(tableName);

        if (maxMessages <= 0) {
            throw new IllegalArgumentException("maxMessages must be greater than 0");
        }

        if (autoCreateTable) {
            createTableIfNecessary();
            createIndexesIfNecessary();
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
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            String databaseProductName = connection.getMetaData().getDatabaseProductName();

            String contentColumnType = resolveContentColumnType(databaseProductName);
            String createdAtColumn = resolveCreatedAtColumn(databaseProductName);

            String sql = """
                CREATE TABLE IF NOT EXISTS %s (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    conversation_id VARCHAR(255) NOT NULL,
                    role VARCHAR(50) NOT NULL,
                    content %s NOT NULL,
                    created_at %s
                )
                """.formatted(tableName, contentColumnType, createdAtColumn);

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
        if (dataSource != null) {
            return dataSource.getConnection();
        }

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
        private DataSource dataSource;

        public Builder dataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

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

    public int count(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return 0;
        }

        String sql = """
            SELECT COUNT(*) AS total
            FROM %s
            WHERE conversation_id = ?
            """.formatted(tableName);

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, conversationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total");
                }
            }

            return 0;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to count JDBC chat memory", e);
        }
    }

    public List<String> listConversationIds() {
        String sql = """
            SELECT DISTINCT conversation_id
            FROM %s
            ORDER BY conversation_id ASC
            """.formatted(tableName);

        List<String> conversationIds = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                conversationIds.add(resultSet.getString("conversation_id"));
            }

            return conversationIds;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to list JDBC conversation ids", e);
        }
    }

    public int deleteAll() {
        String countSql = """
            SELECT COUNT(*) AS total
            FROM %s
            """.formatted(tableName);

        String deleteSql = """
            DELETE FROM %s
            """.formatted(tableName);

        try (Connection connection = getConnection()) {
            int total = 0;

            try (PreparedStatement countStatement = connection.prepareStatement(countSql);
                 ResultSet resultSet = countStatement.executeQuery()) {
                if (resultSet.next()) {
                    total = resultSet.getInt("total");
                }
            }

            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
                deleteStatement.executeUpdate();
            }

            return total;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to delete all JDBC chat memory", e);
        }
    }

    private String resolveContentColumnType(String databaseProductName) {
        if (databaseProductName == null) {
            return "CLOB";
        }

        String name = databaseProductName.toLowerCase();

        if (name.contains("mysql") || name.contains("mariadb")) {
            return "LONGTEXT";
        }

        if (name.contains("postgresql")) {
            return "TEXT";
        }

        if (name.contains("h2")) {
            return "CLOB";
        }

        return "CLOB";
    }

    private String resolveCreatedAtColumn(String databaseProductName) {
        if (databaseProductName == null) {
            return "TIMESTAMP NOT NULL";
        }

        String name = databaseProductName.toLowerCase();

        if (name.contains("mysql") || name.contains("mariadb")) {
            return "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP";
        }

        return "TIMESTAMP NOT NULL";
    }

    private String indexName(String columnName) {
        return "idx_" + tableName + "_" + columnName;
    }

    private void createIndexesIfNecessary() {
        try (Connection connection = getConnection()) {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();

            createIndexIfNecessary(
                    connection,
                    databaseProductName,
                    indexName("conversation_id"),
                    "conversation_id"
            );

            createIndexIfNecessary(
                    connection,
                    databaseProductName,
                    indexName("created_at"),
                    "created_at"
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create JDBC chat memory indexes", e);
        }
    }

    private void createIndexIfNecessary(
            Connection connection,
            String databaseProductName,
            String indexName,
            String columnName
    ) throws SQLException {
        if (indexExists(connection, indexName)) {
            return;
        }

        String sql = """
            CREATE INDEX %s
            ON %s (%s)
            """.formatted(indexName, tableName, columnName);

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private boolean indexExists(Connection connection, String indexName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();

        try (ResultSet resultSet = metaData.getIndexInfo(
                connection.getCatalog(),
                null,
                tableName,
                false,
                false
        )) {
            while (resultSet.next()) {
                String existingIndexName = resultSet.getString("INDEX_NAME");

                if (existingIndexName != null && existingIndexName.equalsIgnoreCase(indexName)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void validateTableName(String tableName) {
        if (!tableName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new IllegalArgumentException(
                    "Invalid tableName: " + tableName +
                            ". Only letters, numbers and underscores are allowed, and it must not start with a number."
            );
        }
    }
}