package edu.yandex.project.wallet.service;

import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

public interface WalletService {

    Mono<Long> getBalance();

    Mono<Long> deposit(@NonNull Long credit);

    Mono<Long> withdraw(@NonNull Long debit);
}

