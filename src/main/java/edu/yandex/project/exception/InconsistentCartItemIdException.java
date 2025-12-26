package edu.yandex.project.exception;

public class InconsistentCartItemIdException extends AbstractProjectException {
    private final static String ERROR_MESSAGE_PATTERN = "Cart.id or Item.id is null";

    public InconsistentCartItemIdException() {
        super(ERROR_MESSAGE_PATTERN);
    }
}
