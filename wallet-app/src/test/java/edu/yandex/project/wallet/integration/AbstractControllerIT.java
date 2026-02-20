package edu.yandex.project.wallet.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractControllerIT {

    @Value("${wallet.amount}")
    protected Long testWalletAmount;

    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected WebTestClient webTestClient;
}
