package edu.yandex.project;

import edu.yandex.project.config.testcontainers.ITPostgreSQLContainer;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ImportTestcontainers(ITPostgreSQLContainer.class)
public abstract class AbstractDbIT {
}
