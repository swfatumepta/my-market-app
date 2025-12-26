package edu.yandex.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

import java.text.MessageFormat;

public class OrderNotFoundException extends AbstractProjectException {
    private final static HttpStatus DEFAULT_HTTP_STATUS = HttpStatus.NOT_FOUND;
    private final static String ERROR_MESSAGE_PATTERN = "OrderEntity.id = {0} not found";

    public OrderNotFoundException(@NonNull Long itemId) {
        super(DEFAULT_HTTP_STATUS, MessageFormat.format(ERROR_MESSAGE_PATTERN, itemId));
    }
}
