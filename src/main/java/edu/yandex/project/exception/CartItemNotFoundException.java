package edu.yandex.project.exception;

import org.springframework.http.HttpStatus;

import java.text.MessageFormat;

public class CartItemNotFoundException extends AbstractProjectException {
    private final static HttpStatus DEFAULT_HTTP_STATUS = HttpStatus.NOT_FOUND;
    private final static String ERROR_MESSAGE_PATTERN = "ItemEntity.id = {0} not found in CartEntity.id = {1}";

    public CartItemNotFoundException(Long cartId, Long itemId) {
        super(DEFAULT_HTTP_STATUS, MessageFormat.format(ERROR_MESSAGE_PATTERN, itemId, cartId));
    }
}
