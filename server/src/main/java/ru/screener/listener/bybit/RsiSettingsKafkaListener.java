package ru.screener.listener.bybit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.screener.config.kafka.bybit.KafkaRsiSettingsTopicProperties;
import ru.screener.listener.AbstractKafkaListener;
import ru.screener.model.kafka.bybit.settings.RsiSettingsEvent;

@Component
@Slf4j
public class RsiSettingsKafkaListener extends AbstractKafkaListener<RsiSettingsEvent> {

    private final KafkaRsiSettingsTopicProperties topicProperties;

    public RsiSettingsKafkaListener(KafkaRsiSettingsTopicProperties topicProperties,
                            ObjectMapper objectMapper) {
        super(objectMapper, RsiSettingsEvent.class);
        this.topicProperties = topicProperties;
    }

    @KafkaListener(
            topics = "${spring.kafka.topic.user.name}",
            groupId = "strategy-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenBybitTopic(String message, Acknowledgment ack) {
        RsiSettingsEvent rsiEvent = getEvent(message);
        log.info("Received a message from {}: {}", topicProperties.getName(), rsiEvent);

        ack.acknowledge();
    }
}
