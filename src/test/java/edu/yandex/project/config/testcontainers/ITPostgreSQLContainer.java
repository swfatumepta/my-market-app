package edu.yandex.project.config.testcontainers;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public class ITPostgreSQLContainer extends PostgreSQLContainer<ITPostgreSQLContainer> {
    private static final String IMAGE_VERSION = "postgres:16.11-alpine3.22";
    private static final String DATABASE_NAME = "ya_market";
    private static final String USERNAME = "test-user";
    private static final String PASSWORD = "test-pass";

    @Container
    @ServiceConnection
    public static ITPostgreSQLContainer CONTAINER = new ITPostgreSQLContainer()
            .withDatabaseName(DATABASE_NAME)
            .withUsername(USERNAME)
            .withPassword(PASSWORD);

    public ITPostgreSQLContainer() {
        super(IMAGE_VERSION);
    }
}
