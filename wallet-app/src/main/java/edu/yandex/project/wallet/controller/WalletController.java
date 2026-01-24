package edu.yandex.project.wallet.controller;

import edu.yandex.project.wallet.api.WalletApi;
import edu.yandex.project.wallet.api.dto.BalanceChangeRequest;
import edu.yandex.project.wallet.api.dto.BalanceResponse;
import edu.yandex.project.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
@RestController
public class WalletController implements WalletApi {

    private final WalletService walletService;

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(ServerWebExchange exchange) {
        log.info("WalletController::getBalance in");
        return walletService.getBalance()
                .map(balance -> ResponseEntity.ok(new BalanceResponse().balance(balance)))
                .doOnSuccess(response ->
                        log.info("WalletController::getBalance out. Result: {}", response)
                );
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> deposit(Mono<BalanceChangeRequest> depositRequest,
                                                         ServerWebExchange exchange) {
        log.info("WalletController::deposit in");
        return depositRequest.map(BalanceChangeRequest::getAmount)
                .flatMap(walletService::deposit)
                .map(balance -> ResponseEntity.ok(new BalanceResponse().balance(balance)))
                .doOnSuccess(response ->
                        log.info("WalletController::deposit out. Result: {}", response)
                );
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> withdraw(Mono<BalanceChangeRequest> withdrawRequest,
                                                          ServerWebExchange exchange) {
        log.info("WalletController::withdraw in");
        return withdrawRequest.map(BalanceChangeRequest::getAmount)
                .flatMap(walletService::withdraw)
                .map(balance -> ResponseEntity.ok(new BalanceResponse().balance(balance)))
                .doOnSuccess(response ->
                        log.info("WalletController::withdraw out. Result: {}", response)
                );
    }
}
