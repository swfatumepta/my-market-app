package edu.yandex.project.integration.config;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;

@SuppressWarnings("resource")
public class ITRedisContainer extends RedisContainer {
    private static final String IMAGE_VERSION = "valkey/valkey:8.1.5-alpine3.23";

    @Container
    @ServiceConnection
    public static RedisContainer CONTAINER = new ITRedisContainer()
            .withCreateContainerCmdModifier(cmd -> cmd.withName("valkey-container-test"));

    public ITRedisContainer() {
        super(IMAGE_VERSION);
    }
}
