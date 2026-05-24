package com.vertexflow.ai.memory;

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
}