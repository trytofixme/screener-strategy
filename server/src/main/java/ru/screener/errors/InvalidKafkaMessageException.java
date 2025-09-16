package ru.screener.errors;

public class InvalidKafkaMessageException extends RuntimeException {

    public InvalidKafkaMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
