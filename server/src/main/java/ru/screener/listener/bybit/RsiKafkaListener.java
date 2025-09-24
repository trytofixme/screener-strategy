package ru.screener.listener.bybit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.screener.dto.bybit.strategies.RsiDto;
import ru.screener.errors.InvalidKafkaMessageException;
import ru.screener.listener.AbstractKafkaListener;
import ru.screener.service.deduplication.CaffeineDeduplicationService;
import ru.screener.service.strategy.StrategyProcessor;

@Component
@Slf4j
public class RsiKafkaListener extends AbstractKafkaListener<RsiDto> {

    private final StrategyProcessor<RsiDto> strategyProcessor;
    private final CaffeineDeduplicationService deduplicationService;

    public RsiKafkaListener(StrategyProcessor<RsiDto> strategyProcessor,
                            CaffeineDeduplicationService deduplicationService,
                            ObjectMapper objectMapper) {
        super(objectMapper, RsiDto.class);
        this.strategyProcessor = strategyProcessor;
        this.deduplicationService = deduplicationService;
    }

    @KafkaListener(topics = "${app.kafka.topic.market.name}", containerFactory = "kafkaListenerContainerFactory")
    public void listenBybitTopic(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.info("Get message {}", record.value());
        try {
            RsiDto rsiDto = getEvent(record.value());
            log.info("Received a message from {}: {}", record.topic(), rsiDto);
            if (!deduplicationService.firstTime(rsiDto.getCrc32())) {
                log.info("Duplicate message from {}: {}", record.topic(), rsiDto);
                return;
            }

            strategyProcessor.process(rsiDto);
            ack.acknowledge();
        } catch (InvalidKafkaMessageException ex) {
            log.error(ex.getMessage());
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Error processing record: {}", record, ex);
        }
    }
}
