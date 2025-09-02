package ru.screener.producer.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.screener.config.kafka.bybit.KafkaRsiSettingsTopicProperties;
import ru.screener.model.kafka.bybit.notification.NotificationEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaRsiSettingsTopicProperties topicProperties;

    public void sendMessage(NotificationEvent notificationDto) {
        kafkaTemplate.send(topicProperties.getName(), notificationDto).thenAccept(arg ->
                        log.info("Notification {} sent to Kafka topic {}", notificationDto, topicProperties.getName()))
        .exceptionally(ex -> {
            log.error("Failed to send notification to Kafka topic '{}'. Error: {}",
            topicProperties.getName(), ex.getMessage());
            return null;
        });
    }
}
