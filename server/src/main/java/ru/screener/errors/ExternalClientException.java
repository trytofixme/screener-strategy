package ru.screener.errors;

public class ExternalClientException extends RuntimeException {

    public ExternalClientException(Throwable cause) {
        super(cause);
    }
}
