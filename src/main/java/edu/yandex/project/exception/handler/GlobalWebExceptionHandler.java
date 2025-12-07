package edu.yandex.project.exception.handler;

import edu.yandex.project.exception.AbstractProjectException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalWebExceptionHandler {
    private final static String ERR_DIR_NAME = "/error/";
    private final static String ERR_MESSAGE_KEY = "errorMessage";

    @ExceptionHandler(AbstractProjectException.class)
    protected String handleItemNotFoundException(AbstractProjectException exc, Model model) {
        log.warn("GlobalWebExceptionHandler::handleItemNotFoundException {} in", exc.toString());
        model.addAttribute(ERR_MESSAGE_KEY, exc.getMessage());
        log.debug("GlobalWebExceptionHandler::handleItemNotFoundException {} out", exc.toString());
        return ERR_DIR_NAME + exc.getHttpStatus().value();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected String handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exc, Model model) {
        log.warn("GlobalWebExceptionHandler::handleHttpRequestMethodNotSupportedException {} in", exc.toString());
        model.addAttribute(ERR_MESSAGE_KEY, exc.getMessage());
        log.debug("GlobalWebExceptionHandler::handleHttpRequestMethodNotSupportedException {} out", exc.toString());
        return ERR_DIR_NAME + HttpStatus.METHOD_NOT_ALLOWED.value();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected String handleMethodArgumentNotValidException(MethodArgumentNotValidException exc, Model model) {
        log.warn("GlobalWebExceptionHandler::handleMethodArgumentNotValidException {} in", exc.toString());
        model.addAttribute(ERR_MESSAGE_KEY, exc.getMessage());
        log.debug("GlobalWebExceptionHandler::handleMethodArgumentNotValidException {} out", exc.toString());
        return ERR_DIR_NAME + HttpStatus.BAD_REQUEST.value();
    }

    @ExceptionHandler(RuntimeException.class)
    protected String handleRuntimeException(RuntimeException exc, Model model) {
        log.warn("GlobalWebExceptionHandler::handleRuntimeException {} in", exc.toString());
        model.addAttribute(ERR_MESSAGE_KEY, exc.getMessage());
        log.debug("GlobalWebExceptionHandler::handleRuntimeException {} out", exc.toString());
        return ERR_DIR_NAME + HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
