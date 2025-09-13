package ru.screener.listener.bybit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.screener.config.kafka.bybit.KafkaRsiTopicProperties;
import ru.screener.listener.AbstractKafkaListener;
import ru.screener.model.kafka.bybit.strategies.RsiEvent;
import ru.screener.service.strategy.StrategyProcessor;

@Component
@Slf4j
public class RsiKafkaListener extends AbstractKafkaListener<RsiEvent> {

    private final StrategyProcessor<RsiEvent> strategyProcessor;
    private final KafkaRsiTopicProperties topicProperties;

    public RsiKafkaListener(StrategyProcessor<RsiEvent> strategyProcessor,
                            KafkaRsiTopicProperties topicProperties,
                            ObjectMapper objectMapper) {
        super(objectMapper, RsiEvent.class);
        this.strategyProcessor = strategyProcessor;
        this.topicProperties = topicProperties;
    }

    @KafkaListener(
            topics = "${app.kafka.topic.market.name}",
            groupId = "strategy-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenBybitTopic(String message, Acknowledgment ack) {
        log.info("Get message {}", message);
        RsiEvent rsiEvent = getEvent(message);
        if (rsiEvent == null || rsiEvent.getSymbol() == null) {
            log.error("Failed to deserialize message or message is incomplete. Original message: {}. Deserialized object: {}", message, rsiEvent);
            ack.acknowledge();
            return;
        }

        log.info("Received a message from {}: {}", topicProperties.getName(), rsiEvent);

        try {
            strategyProcessor.process(rsiEvent);
        } catch (Exception e) {
            log.error("Error processing event: {}", rsiEvent, e);
        }

        ack.acknowledge();
    }
}
