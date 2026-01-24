package edu.yandex.project.wallet.exception;

import org.springframework.http.HttpStatus;

import java.text.MessageFormat;

public class InsufficientFundsException extends RuntimeException {
    private final static HttpStatus HTTP_STATUS = HttpStatus.CONFLICT;
    private final static String MESSAGE_PATTERN = "Insufficient funds -> balance = {0}; withdraw = {1}";

    public InsufficientFundsException(long balance, long debit) {
        super(MessageFormat.format(MESSAGE_PATTERN, balance, debit));
    }

    public HttpStatus getHttpStatus() {
        return HTTP_STATUS;
    }
}
