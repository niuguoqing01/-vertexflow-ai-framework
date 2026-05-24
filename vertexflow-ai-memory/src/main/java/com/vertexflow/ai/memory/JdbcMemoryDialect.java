package com.vertexflow.ai.memory;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcMemoryDialect {

    private final String databaseProductName;

    private JdbcMemoryDialect(String databaseProductName) {
        this.databaseProductName = databaseProductName == null
                ? ""
                : databaseProductName.toLowerCase();
    }

    public static JdbcMemoryDialect of(String databaseProductName) {
        return new JdbcMemoryDialect(databaseProductName);
    }

    public String contentColumnType() {
        if (isMySql() || isMariaDb()) {
            return "LONGTEXT";
        }

        if (isPostgreSql()) {
            return "TEXT";
        }

        if (isH2()) {
            return "CLOB";
        }

        return "CLOB";
    }

    public String createdAtColumnDefinition() {
        if (isMySql() || isMariaDb()) {
            return "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP";
        }

        return "TIMESTAMP NOT NULL";
    }

    public boolean supportsCreateIndexIfNotExists() {
        return isPostgreSql();
    }

    public String createIndexSql(String indexName, String tableName, String columnName) {
        if (supportsCreateIndexIfNotExists()) {
            return """
                    CREATE INDEX IF NOT EXISTS %s
                    ON %s (%s)
                    """.formatted(indexName, tableName, columnName);
        }

        return """
                CREATE INDEX %s
                ON %s (%s)
                """.formatted(indexName, tableName, columnName);
    }

    private boolean isMySql() {
        return databaseProductName.contains("mysql");
    }

    private boolean isMariaDb() {
        return databaseProductName.contains("mariadb");
    }

    private boolean isPostgreSql() {
        return databaseProductName.contains("postgresql");
    }

    private boolean isH2() {
        return databaseProductName.contains("h2");
    }

    public String idColumnDefinition() {
        if (isPostgreSql()) {
            return "BIGSERIAL PRIMARY KEY";
        }

        return "BIGINT AUTO_INCREMENT PRIMARY KEY";
    }

    public String selectRecentMessagesSql(String tableName) {
        if (isSqlServer()) {
            return """
                SELECT TOP (?) role, content
                FROM %s
                WHERE conversation_id = ?
                ORDER BY id DESC
                """.formatted(tableName);
        }

        if (isOracle()) {
            return """
                SELECT role, content
                FROM (
                    SELECT role, content
                    FROM %s
                    WHERE conversation_id = ?
                    ORDER BY id DESC
                )
                WHERE ROWNUM <= ?
                """.formatted(tableName);
        }

        return """
            SELECT role, content
            FROM %s
            WHERE conversation_id = ?
            ORDER BY id DESC
            LIMIT ?
            """.formatted(tableName);
    }

    private boolean isSqlServer() {
        return databaseProductName.contains("microsoft sql server");
    }

    private boolean isOracle() {
        return databaseProductName.contains("oracle");
    }

    public void bindSelectRecentMessagesParams(
            PreparedStatement statement,
            String conversationId,
            int maxMessages
    ) throws SQLException {
        if (isSqlServer()) {
            statement.setInt(1, maxMessages);
            statement.setString(2, conversationId);
            return;
        }

        statement.setString(1, conversationId);
        statement.setInt(2, maxMessages);
    }
}