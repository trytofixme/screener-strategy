package ru.screener.service.strategy.rsi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.screener.model.kafka.bybit.notification.NotificationEvent;
import ru.screener.model.kafka.bybit.settings.RsiSettingsEvent;
import ru.screener.model.kafka.bybit.strategies.RsiEvent;
import ru.screener.model.strategies.Direction;
import ru.screener.model.strategies.Strategy;
import ru.screener.producer.notification.NotificationPublisher;
import ru.screener.service.strategy.StrategyProcessor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.math.BigDecimal.ZERO;

@Slf4j
@Service
@RequiredArgsConstructor
public class RsiProcessor implements StrategyProcessor<RsiEvent> {

    private static final BigDecimal RSI_MAX = BigDecimal.valueOf(100.0);

    private final NotificationPublisher notificationPublisher;
    private final Map<Long, List<RsiSettingsEvent>> userStrategies = new ConcurrentHashMap<>();
    private final Map<String, Integer> strategyCounts = new ConcurrentHashMap<>();

    @Override
    public void process(RsiEvent rsiEvent) {
        log.info("process RsiEvent : {}", rsiEvent);
        userStrategies.forEach((userId, settings) -> {
            for (RsiSettingsEvent setting : settings) {
                if (!isValidRsi(rsiEvent.getRsi())) {
                    continue;
                }

                if (!rsiEvent.getTimeframe().equals(setting.getShortTimeFrame())
                        && !rsiEvent.getTimeframe().equals(setting.getLongTimeFrame())) {
                    continue;
                }

                int strategyCount = strategyCounts.getOrDefault(rsiEvent.getSymbol(), 0);
                boolean shortStrategy = rsiEvent.getTimeframe().equals(setting.getShortTimeFrame())
                        && rsiEvent.getRsi().compareTo(setting.getShortRsi()) > 0;
                boolean longStrategy = rsiEvent.getTimeframe().equals(setting.getLongTimeFrame())
                        && rsiEvent.getRsi().compareTo(setting.getLongRsi()) < 0;

                if (shortStrategy || longStrategy) {
                    NotificationEvent notification = new NotificationEvent()
                            .setTelegramId(setting.getTelegramId())
                            .setStrategy(Strategy.RSI)
                            .setSymbol(rsiEvent.getSymbol())
                            .setTimeframe(rsiEvent.getTimeframe())
                            .setDirection(shortStrategy ? Direction.DOWN : Direction.UP)
                            .setRsi(rsiEvent.getRsi())
                            .setSignalNumber(strategyCount);

                    notificationPublisher.sendMessage(notification);
                    strategyCounts.put(rsiEvent.getSymbol(), strategyCount + 1);
                }
            }
        });
    }

    public void updateUserStrategy(RsiSettingsEvent setting) {
        userStrategies.compute(setting.getTelegramId(), (user, list) -> {
            List<RsiSettingsEvent> updated = (list != null) ? new ArrayList<>(list) : new ArrayList<>();
            updated.add(setting);
            return updated;
        });
        log.info("userStrategies: {}", userStrategies);
    }

    public Mono<Void> loadInitialSettings(Flux<RsiSettingsEvent> settingsFlux) {
        return settingsFlux
                .doOnNext(this::updateUserStrategy)
                .then();
    }

    private boolean isValidRsi(BigDecimal rsiValue) {
        return rsiValue != null && rsiValue.compareTo(ZERO) > 0 && rsiValue.compareTo(RSI_MAX) <= 0;
    }

    @Scheduled(
            cron = "${app.rsi.strategy-counts.reset-cron:0 0 0 * * *}",
            zone = "${app.timezone:Europe/Amsterdam}"
    )
    private void resetDailyStrategyCounts() {
        int before = strategyCounts.size();
        strategyCounts.clear();
        log.info("Сброс strategyCounts: очищено {} ключей", before);
    }
}
