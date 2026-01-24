package edu.yandex.project.exception;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public abstract class AbstractProjectException extends RuntimeException {
    private final static HttpStatus DEFAULT_HTTP_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

    private final HttpStatus httpStatus;
    private final String message;

    public AbstractProjectException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public AbstractProjectException(String message) {
        this(DEFAULT_HTTP_STATUS, message);
    }
}
