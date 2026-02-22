package ru.screener.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.Acknowledgment;
import ru.screener.dto.bybit.strategies.RsiDto;
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

    protected void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack){
        log.info("Get message {}", record.value());
        try {
            T rsiDto = getEvent(record.value());
            log.info("Received a message from {}: {}", record.topic(), rsiDto);

            handle(rsiDto);
            ack.acknowledge();
        } catch (InvalidKafkaMessageException ex) {
            log.error(ex.getMessage());
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Error processing record: {}", record, ex);
        }
    }

    protected abstract void handle(T dto);
}
