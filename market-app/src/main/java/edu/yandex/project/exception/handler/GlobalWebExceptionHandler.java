package edu.yandex.project.exception.handler;

import edu.yandex.project.exception.AbstractProjectException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

@ControllerAdvice
@Slf4j
public class GlobalWebExceptionHandler {
    private final static String ERR_DIR_NAME = "/error/";
    private final static String ERR_MESSAGE_KEY = "errorMessage";
    private final static String VALUE_REJECTED_TEMPLATE = "{0}: value rejected = {1}";

    @ExceptionHandler(AbstractProjectException.class)
    public Mono<Rendering> handleAbstractProjectException(AbstractProjectException exc) {
        log.error("GlobalWebExceptionHandler::handleAbstractProjectException {}", exc.getMessage(), exc);
        return Mono.just(Rendering
                .view(ERR_DIR_NAME + exc.getHttpStatus().value())
                .modelAttribute(ERR_MESSAGE_KEY, exc.getMessage())
                .status(exc.getHttpStatus())
                .build());
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public Mono<Rendering> handleHandlerMethodValidationException(HandlerMethodValidationException exc) {
        log.error("GlobalWebExceptionHandler::handleHandlerMethodValidationException {}", exc.getMessage(), exc);
        return Mono.just(Rendering
                .view(ERR_DIR_NAME + exc.getStatusCode().value())
                .modelAttribute(ERR_MESSAGE_KEY, buildErrorMessage(exc))
                .status(exc.getStatusCode())
                .build());
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<Rendering> handleWebExchangeBindException(WebExchangeBindException exc) {
        log.error("GlobalWebExceptionHandler::handleWebExchangeBindException {}", exc.getMessage(), exc);
        return Mono.just(Rendering
                .view(ERR_DIR_NAME + exc.getStatusCode().value())
                .modelAttribute(ERR_MESSAGE_KEY, buildErrorMessage(exc))
                .status(exc.getStatusCode())
                .build());
    }

    @ExceptionHandler(ErrorResponseException.class)
    public Mono<Rendering> handleErrorResponseException(ErrorResponseException exc) {
        log.error("GlobalWebExceptionHandler::handleErrorResponseException {}", exc.getMessage(), exc);
        return Mono.just(Rendering
                .view(ERR_DIR_NAME + exc.getStatusCode().value())
                .modelAttribute(ERR_MESSAGE_KEY, exc.getMessage())
                .status(exc.getStatusCode())
                .build());
    }

    @ExceptionHandler(RuntimeException.class)
    public Mono<Rendering> handleRuntimeException(RuntimeException exc) {
        log.error("GlobalWebExceptionHandler::handleRuntimeException {}", exc.getMessage(), exc);
        return Mono.just(Rendering
                .view(ERR_DIR_NAME + HttpStatus.INTERNAL_SERVER_ERROR.value())
                .modelAttribute(ERR_MESSAGE_KEY, exc.getMessage())
                .build());
    }

    private static String buildErrorMessage(HandlerMethodValidationException exc) {
        return exc.getParameterValidationResults().stream()
                .map(ParameterValidationResult::getResolvableErrors)
                .flatMap(List::stream)
                .map(messageSourceResolvable -> {
                    if (messageSourceResolvable instanceof FieldError fieldError) {
                        var field = fieldError.getField();
                        var error = Objects.requireNonNullElse(fieldError.getDefaultMessage(), "");
                        return field + ": " + error;
                    }
                    return "";
                })
                .collect(Collectors.joining(", "));
    }

    private static String buildErrorMessage(WebExchangeBindException exc) {
        return exc.getFieldErrors().stream()
                .map(fieldError -> format(VALUE_REJECTED_TEMPLATE, fieldError.getField(), fieldError.getRejectedValue()))
                .collect(Collectors.joining(", "));
    }
}
