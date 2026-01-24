package edu.yandex.project.wallet.service.impl;

import edu.yandex.project.wallet.exception.InsufficientFundsException;
import edu.yandex.project.wallet.repository.WalletRepository;
import edu.yandex.project.wallet.service.WalletService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    @Override
    public Mono<Long> getBalance() {
        log.debug("WalletServiceImpl::getBalance in");
        return Mono.fromSupplier(walletRepository::get)
                .doOnSuccess(result -> log.debug("WalletServiceImpl::getBalance out. Result: {}", result));
    }

    @Override
    public Mono<Long> deposit(@NonNull Long credit) {
        log.debug("WalletServiceImpl::deposit {} in", credit);
        return Mono.fromSupplier(() -> walletRepository.update(walletRepository.get() + credit))
                .doOnSuccess(result -> log.debug("WalletServiceImpl::deposit {} out. Result: {}", credit, result));
    }

    @Override
    public Mono<Long> withdraw(@NonNull Long debit) {
        log.debug("WalletServiceImpl::withdraw {} in", debit);
        return Mono.fromSupplier(() -> {
                    var currentBalance = walletRepository.get();
                    if (currentBalance < debit) {
                        throw new InsufficientFundsException(currentBalance, debit);
                    }
                    return walletRepository.update(currentBalance - debit);
                })
                .doOnSuccess(result -> log.debug("WalletServiceImpl::withdraw {} out. Result: {}", debit, result));
    }
}
