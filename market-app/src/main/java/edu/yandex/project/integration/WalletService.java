package edu.yandex.project.integration;

import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

public interface WalletService {

    Mono<Long> getBalance();

    Mono<Void> withdraw(@NonNull Long amount);
}
