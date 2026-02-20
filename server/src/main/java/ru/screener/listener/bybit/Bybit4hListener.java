package ru.screener.listener.bybit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.screener.dto.bybit.strategies.RsiDto;
import ru.screener.listener.AbstractKafkaListener;
import ru.screener.service.deduplication.CaffeineDeduplicationService;
import ru.screener.service.strategy.StrategyProcessor;

@Component
@Slf4j
public class Bybit4hListener extends AbstractKafkaListener<RsiDto> {

    private final StrategyProcessor<RsiDto> strategyProcessor;
    private final CaffeineDeduplicationService deduplicationService;

    public Bybit4hListener(StrategyProcessor<RsiDto> strategyProcessor,
                           CaffeineDeduplicationService deduplicationService,
                           ObjectMapper objectMapper) {
        super(objectMapper, RsiDto.class);
        this.strategyProcessor = strategyProcessor;
        this.deduplicationService = deduplicationService;
    }

    @KafkaListener(topics = "${app.kafka.topic.bybit4h.name}",
            containerFactory = "kafkaListenerContainerFactory")
    public void listen(ConsumerRecord<String, String> record, Acknowledgment ack){
        onMessage(record, ack);
    }

    @Override
    protected void handle(RsiDto dto) {
        if (!deduplicationService.firstTime(dto.getCrc32())) {
            log.info("Duplicate message : {}", dto);
            return;
        }
        strategyProcessor.process(dto);
    }
}
