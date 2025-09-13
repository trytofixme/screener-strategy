package ru.screener.service.strategy.rsi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RsiProcessor implements StrategyProcessor<RsiEvent> {

    private final NotificationPublisher notificationPublisher;
    private final Map<Long, List<RsiSettingsEvent>> userStrategies = new ConcurrentHashMap<>();
    private final Map<Long, Integer> strategyCounts = new ConcurrentHashMap<>();

    @Override
    public void process(RsiEvent rsiEvent) {
        userStrategies.forEach((userId, settings) -> {
            for (RsiSettingsEvent setting : settings) {
                if (!rsiEvent.getTimeframe().equals(setting.getShortTimeFrame())
                        && !rsiEvent.getTimeframe().equals(setting.getLongTimeFrame())) {
                    continue;
                }

                if (rsiEvent.getTimeframe().equals(setting.getShortTimeFrame())
                        && rsiEvent.getRsi().compareTo(setting.getShortRsi()) > 0) {

                    int strategyCount = strategyCounts.getOrDefault(setting.getTelegramId(), 0);

                    NotificationEvent notification = new NotificationEvent()
                            .setTelegramId(setting.getTelegramId())
                            .setStrategy(Strategy.RSI)
                            .setSymbol(rsiEvent.getSymbol())
                            .setTimeframe(rsiEvent.getTimeframe())
                            .setDirection(Direction.DOWN)
                            .setRsi(rsiEvent.getRsi())
                            .setSignalNumber(strategyCount);

                    notificationPublisher.sendMessage(notification);
                    strategyCounts.put(setting.getTelegramId(), strategyCount + 1);
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
    }

    public Mono<Void> loadInitialSettings(Flux<RsiSettingsEvent> settingsFlux) {
        return settingsFlux
                .doOnNext(this::updateUserStrategy)
                .then();
    }
}
