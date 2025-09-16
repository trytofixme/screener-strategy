package ru.screener.listener.bybit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.screener.cache.rsi.RsiSettingsCache;
import ru.screener.dto.bybit.settings.RsiSettingsDto;
import ru.screener.errors.InvalidKafkaMessageException;
import ru.screener.listener.AbstractKafkaListener;

@Component
@Slf4j
public class RsiSettingsKafkaListener extends AbstractKafkaListener<RsiSettingsDto> {

    private final RsiSettingsCache rsiSettingsCache;

    public RsiSettingsKafkaListener(RsiSettingsCache rsiSettingsCache,
                                    ObjectMapper objectMapper) {
        super(objectMapper, RsiSettingsDto.class);
        this.rsiSettingsCache = rsiSettingsCache;
    }

    @KafkaListener(topics = "${app.kafka.topic.user.name}", containerFactory = "kafkaListenerContainerFactory")
    public void listenSettingsTopic(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            RsiSettingsDto rsiSettingsDto = getEvent(record.value());
            log.info("Received a message from {}: {}", record.topic(), rsiSettingsDto);

            rsiSettingsCache.upsertRsiSettings(rsiSettingsDto);
            ack.acknowledge();
        } catch (InvalidKafkaMessageException ex) {
            log.error(ex.getMessage());
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Error processing record: {}", record, ex);
        }
    }
}
