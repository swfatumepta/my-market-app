package edu.yandex.project.exception.handler;

import edu.yandex.project.exception.AbstractProjectException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

@ControllerAdvice
@Slf4j
public class GlobalWebExceptionHandler {
    private final static String ERR_DIR_NAME = "/error/";
    private final static String ERR_MESSAGE_KEY = "errorMessage";
    private final static String ARGUMENT_ERR_TEMPLATE = "{0}: value rejected = {1}";

    @ExceptionHandler(AbstractProjectException.class)
    protected String handleItemNotFoundException(AbstractProjectException exc, Model model) {
        log.error("GlobalWebExceptionHandler::handleItemNotFoundException {} in", exc.toString());
        model.addAttribute(ERR_MESSAGE_KEY, exc.getMessage());
        log.debug("GlobalWebExceptionHandler::handleItemNotFoundException {} out", exc.toString());
        return ERR_DIR_NAME + exc.getHttpStatus().value();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected String handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exc, Model model) {
        log.error("GlobalWebExceptionHandler::handleHttpRequestMethodNotSupportedException {} in", exc.toString());
        model.addAttribute(ERR_MESSAGE_KEY, exc.getMessage());
        log.debug("GlobalWebExceptionHandler::handleHttpRequestMethodNotSupportedException {} out", exc.toString());
        return ERR_DIR_NAME + HttpStatus.METHOD_NOT_ALLOWED.value();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected String handleMethodArgumentNotValidException(MethodArgumentNotValidException exc, Model model) {
        log.warn("GlobalWebExceptionHandler::handleMethodArgumentNotValidException {} in", exc.toString());
        model.addAttribute(ERR_MESSAGE_KEY, buildErrorMessage(exc));
        log.debug("GlobalWebExceptionHandler::handleMethodArgumentNotValidException {} out", exc.toString());
        return ERR_DIR_NAME + HttpStatus.BAD_REQUEST.value();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected String handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exc, Model model) {
        log.warn("GlobalWebExceptionHandler::handleMethodArgumentTypeMismatchException {} in", exc.toString());
        model.addAttribute(ERR_MESSAGE_KEY, buildErrorMessage(exc));
        log.debug("GlobalWebExceptionHandler::handleMethodArgumentTypeMismatchException {} out", exc.toString());
        return ERR_DIR_NAME + HttpStatus.BAD_REQUEST.value();
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    protected String handleHandlerMethodValidationException(HandlerMethodValidationException exc, Model model) {
        log.error("GlobalWebExceptionHandler::handleHandlerMethodValidationException {} in", exc.getDetailMessageArguments());
        model.addAttribute(ERR_MESSAGE_KEY, exc.getDetailMessageArguments());
        log.debug("GlobalWebExceptionHandler::handleHandlerMethodValidationException {} out", exc.getDetailMessageArguments());
        return ERR_DIR_NAME + HttpStatus.BAD_REQUEST.value();
    }

    @ExceptionHandler(RuntimeException.class)
    protected String handleRuntimeException(RuntimeException exc, Model model) {
        log.error("GlobalWebExceptionHandler::handleRuntimeException {} in", exc.toString());
        model.addAttribute(ERR_MESSAGE_KEY, exc.getMessage());
        log.debug("GlobalWebExceptionHandler::handleRuntimeException {} out", exc.toString());
        return ERR_DIR_NAME + HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    private static String[] buildErrorMessage(MethodArgumentTypeMismatchException exc) {
        return new String[]{format(ARGUMENT_ERR_TEMPLATE, exc.getName(), exc.getValue())};
    }

    private static String[] buildErrorMessage(MethodArgumentNotValidException exc) {
        return new String[]{
                exc.getFieldErrors().stream()
                        .map(fieldError -> format(ARGUMENT_ERR_TEMPLATE, fieldError.getField(), fieldError.getRejectedValue()))
                        .collect(Collectors.joining(", "))
        };
    }
}
