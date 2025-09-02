package ru.screener.errors.client;

public class RemoteNotFoundException extends RuntimeException {
    public RemoteNotFoundException(String msg) {
        super(msg);
    }
}
