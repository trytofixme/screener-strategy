package ru.screener.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.screener.config.kafka.KafkaQuotesTopicProperties;
import ru.screener.dto.notification.NotificationDto;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaQuotesTopicProperties topicProperties;

    public void sendMessage(NotificationDto notificationDto) {
        kafkaTemplate.send(topicProperties.getName(), notificationDto).thenAccept(arg ->
                        log.info("Notification {} sent to Kafka topic {}", notificationDto, topicProperties.getName()))
        .exceptionally(ex -> {
            log.error("Failed to send notification to Kafka topic '{}'. Error: {}",
            topicProperties.getName(), ex.getMessage());
            return null;
        });
    }
}
