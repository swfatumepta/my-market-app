package edu.yandex.project.wallet.exception.handler;

import edu.yandex.project.wallet.api.dto.ErrorResponse;
import edu.yandex.project.wallet.exception.InsufficientFundsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientFundsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInsufficientFunds(InsufficientFundsException exc) {
        log.error("InsufficientFundsException handled: {}", exc.getMessage(), exc);
        return Mono.just(ResponseEntity
                .status(exc.getHttpStatus())
                .body(new ErrorResponse(exc.getMessage(), exc.getHttpStatus().value()))
        );
    }
}
