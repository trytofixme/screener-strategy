package ru.screener.producer.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.screener.config.kafka.notification.KafkaNotificationTopicProperties;
import ru.screener.dto.bybit.notification.NotificationDto;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaNotificationTopicProperties topicProperties;

    public void sendMessage(NotificationDto notificationDto) {
        String key = String.valueOf(notificationDto.getTelegramId());
        kafkaTemplate.send(topicProperties.getName(), key, notificationDto)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send to {} key={} dto={}", topicProperties.getName(), key, notificationDto, ex);
                } else {
                    var metadata = result.getRecordMetadata();
                    log.debug("Sent to {}-{}@{} key={}", metadata.topic(), metadata.partition(), metadata.offset(), key);
                }
            });
    }
}
