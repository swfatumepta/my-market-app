package edu.yandex.project.exception.handler;

import edu.yandex.project.exception.ItemNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class GlobalWebExceptionHandler {

    @ExceptionHandler(ItemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected String handleItemNotFoundException(ItemNotFoundException exc) {
        log.warn("GlobalWebExceptionHandler::handleItemNotFoundException {} in", exc.toString());
        return "error/stub_404.html";
    }
}
