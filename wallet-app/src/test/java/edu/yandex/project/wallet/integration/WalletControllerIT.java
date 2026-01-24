package edu.yandex.project.wallet.integration;

import edu.yandex.project.wallet.api.dto.BalanceChangeRequest;
import edu.yandex.project.wallet.repository.WalletRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.text.MessageFormat;
import java.util.concurrent.ThreadLocalRandom;

@Tag("WalletControllerIT")
public class WalletControllerIT extends AbstractControllerIT {

    @Autowired
    private WalletRepository walletRepository;

    @BeforeEach
    void reset() {
        walletRepository.update(testWalletAmount);
    }

    @Test
    void getBalance_shouldReturnInitialBalanceFromProperties() {
        // given
        // when
        webTestClient.get()
                .uri("/wallet/balance")
                .exchange()
                // then
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(testWalletAmount);
    }

    @SneakyThrows
    @Test
    void deposit_shouldAddMoneyToDefaultWallet() {
        // given
        var deposit = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
        var depositRequest = objectMapper.writeValueAsString(new BalanceChangeRequest(deposit));
        // when
        webTestClient.post()
                .uri("/wallet/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(depositRequest)
                .exchange()
                // then
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(testWalletAmount + deposit);
    }

    @SneakyThrows
    @Test
    void deposit_inCaseAmountIsNegative_shouldReturnError400() {
        // given
        var depositRequest = objectMapper.writeValueAsString(new BalanceChangeRequest(-1L));
        // when
        webTestClient.post()
                .uri("/wallet/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(depositRequest)
                .exchange()
                // then
                .expectStatus().isBadRequest();
    }

    @SneakyThrows
    @RepeatedTest(5)
    void withdraw_shouldWithdrawMoney() {
        // given
        var withdraw = ThreadLocalRandom.current().nextLong(1, testWalletAmount);
        var withdrawRequest = objectMapper.writeValueAsString(new BalanceChangeRequest(withdraw));

        var expectedAmount = testWalletAmount - withdraw;
        // when
        webTestClient.post()
                .uri("/wallet/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(withdrawRequest)
                .exchange()
                // then
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.balance").isEqualTo(expectedAmount);
    }

    @SneakyThrows
    @Test
    void withdraw_inCaseAmountIsGreaterThenWalletBalance_shouldReturnError409InsufficientFunds() {
        // given
        var withdraw = testWalletAmount + 1;
        var withdrawRequest = objectMapper.writeValueAsString(new BalanceChangeRequest(withdraw));

        var expectedAmount = MessageFormat.format("Insufficient funds -> balance = {0}; withdraw = {1}",
                testWalletAmount, withdraw);
        // when
        webTestClient.post()
                .uri("/wallet/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(withdrawRequest)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo(expectedAmount);
    }
}
