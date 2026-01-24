package edu.yandex.project.exception;

import org.springframework.http.HttpStatus;

import java.text.MessageFormat;

public class ItemNotFoundException extends AbstractProjectException {
    private final static HttpStatus DEFAULT_HTTP_STATUS = HttpStatus.NOT_FOUND;
    private final static String ERROR_MESSAGE_PATTERN = "ItemEntity.id = {0} not found";

    public ItemNotFoundException(Long itemId) {
        super(DEFAULT_HTTP_STATUS, MessageFormat.format(ERROR_MESSAGE_PATTERN, itemId));
    }
}
