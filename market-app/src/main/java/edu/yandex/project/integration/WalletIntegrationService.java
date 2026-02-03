package edu.yandex.project.integration;

import edu.yandex.project.client.WalletApi;
import edu.yandex.project.client.dto.BalanceChangeRequest;
import edu.yandex.project.client.dto.BalanceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletIntegrationService implements WalletService {

    private final WalletApi walletClient;

    @Override
    public Mono<Long> getBalance() {
        log.debug("WalletServiceImpl::getBalance in");
        return walletClient.getBalance()
                .map(BalanceResponse::getBalance)
                .doOnSuccess(balance -> log.debug("WalletServiceImpl::getBalance out. Result: {}", balance));
    }

    @Override
    public Mono<Void> withdraw(@NonNull Long amount) {
        log.debug("WalletServiceImpl::withdraw {} in", amount);
        return walletClient.withdraw(new BalanceChangeRequest().amount(amount))
                .doOnSuccess(response ->
                        log.debug("WalletServiceImpl::withdraw {} out. Wallet balance: {}", amount, response))
                .then();
    }
}
