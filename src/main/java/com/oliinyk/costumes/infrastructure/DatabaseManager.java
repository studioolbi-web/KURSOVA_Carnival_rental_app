package com.oliinyk.costumes.infrastructure;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.Flyway;

public class DatabaseManager {
    private static HikariDataSource dataSource;

    public static void initialize(String url, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(10);

        dataSource = new HikariDataSource(config);

        // Flyway не може сканувати classpath у named JPMS-модулі через обмеження
        // ClassLoader.getResources().
        // Class.getResource() натомість має доступ до ресурсів того ж модуля.
        // Отримуємо filesystem-шлях і передаємо як filesystem: location.
        java.net.URL migrationDirUrl = DatabaseManager.class.getResource("/db/migration/");
        String migrationLocation;
        if (migrationDirUrl != null && "file".equals(migrationDirUrl.getProtocol())) {
            try {
                migrationLocation =
                        "filesystem:"
                                + java.nio.file.Paths.get(migrationDirUrl.toURI()).toAbsolutePath();
            } catch (java.net.URISyntaxException e) {
                migrationLocation = "classpath:db/migration";
            }
        } else {
            // Fallback для запуску з JAR
            migrationLocation = "classpath:db/migration";
        }

        Flyway flyway =
                Flyway.configure(DatabaseManager.class.getClassLoader())
                        .dataSource(dataSource)
                        .locations(migrationLocation)
                        .load();
        flyway.migrate();
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new IllegalStateException("DatabaseManager is not initialized.");
        }
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
