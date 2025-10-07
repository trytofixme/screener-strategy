package ru.screener.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import ru.screener.service.strategy.rsi.RsiProcessor;

@Slf4j
@Component
@RequiredArgsConstructor
public class StrategyCountsResetJob {

    private final RsiProcessor rsiProcessor;
    private final ThreadPoolTaskScheduler strategyCountsScheduler;

    @Scheduled(cron = "${app.rsi.strategy-counts.reset-cron:0 0 0 * * *}",
            zone  = "${app.timezone:Europe/Amsterdam}")
    public void reset() {
        int before = rsiProcessor.resetDailyStrategyCounts();
        log.info("Сброс strategyCounts: очищено {} ключей", before);
    }
}
