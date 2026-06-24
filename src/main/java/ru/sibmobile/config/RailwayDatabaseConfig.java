package ru.sibmobile.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
@Profile("prod")
@ConditionalOnProperty(name = "PGHOST", matchIfMissing = true)
public class RailwayDatabaseConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "DATABASE_URL")
    public DataSource railwayDataSource(@Value("${DATABASE_URL}") String databaseUrl) {
        try {
            URI uri = URI.create(databaseUrl.replace("postgres://", "postgresql://"));

            String username = "";
            String password = "";
            if (uri.getUserInfo() != null) {
                String[] parts = uri.getUserInfo().split(":", 2);
                username = decode(parts[0]);
                if (parts.length > 1) {
                    password = decode(parts[1]);
                }
            }

            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort()
                    + uri.getPath() + "?sslmode=require";

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(5);
            config.setConnectionTimeout(30_000);
            return new HikariDataSource(config);
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось разобрать DATABASE_URL для PostgreSQL", e);
        }
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
