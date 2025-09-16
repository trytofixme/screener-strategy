package ru.screener.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.screener.errors.InvalidKafkaMessageException;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractKafkaListener<T> {

    private final ObjectMapper objectMapper;
    private final Class<T> eventType;

    public T getEvent(String message) {
        try {
            return objectMapper.readValue(message, eventType);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to deserialize Kafka message into class {}. Message: {}. Error: {}",
                    eventType.getSimpleName(), message, ex.getMessage(), ex);
            throw new InvalidKafkaMessageException("Invalid message format", ex);
        }
    }
}
