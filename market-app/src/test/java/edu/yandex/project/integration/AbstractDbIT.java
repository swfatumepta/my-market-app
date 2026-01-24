package edu.yandex.project.integration;

import edu.yandex.project.integration.config.ITPostgreSQLContainer;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ImportTestcontainers(ITPostgreSQLContainer.class)
public abstract class AbstractDbIT {
}
