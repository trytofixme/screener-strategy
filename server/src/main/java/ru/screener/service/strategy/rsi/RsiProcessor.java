package ru.screener.service.strategy.rsi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.screener.cache.rsi.RsiSettingsCache;
import ru.screener.dto.bybit.notification.NotificationDto;
import ru.screener.dto.bybit.strategies.RsiDto;
import ru.screener.model.bybit.settings.RsiSettings;
import ru.screener.model.bybit.strategies.Direction;
import ru.screener.model.bybit.strategies.Strategy;
import ru.screener.producer.notification.NotificationPublisher;
import ru.screener.service.strategy.StrategyProcessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@Slf4j
@Service
@RequiredArgsConstructor
public class RsiProcessor implements StrategyProcessor<RsiDto> {

    private final NotificationPublisher notificationPublisher;
    private final RsiValidator rsiValidator;
    private final RsiSettingsCache rsiSettingsCache;
    private final Map<String, LongAdder> strategyCounts = new ConcurrentHashMap<>();

    @Override
    public void process(RsiDto rsiDto) {
        if (!rsiValidator.isRsiSettingsValid(rsiDto.getRsi())) {
            log.warn("Invalid RSI event: {}", rsiDto);
            return;
        }

        log.info("Process RSI : {}", rsiDto);
        final Map<Long, RsiSettings> rsiSettingsByTgUser = rsiSettingsCache.getAllSettings();
        for (Map.Entry<Long, RsiSettings> settingsEntry : rsiSettingsByTgUser.entrySet()) {
            Long telegramId = settingsEntry.getKey();
            RsiSettings settings = settingsEntry.getValue();

            final String timeFrame = rsiDto.getTimeframe();
            if (!timeFrame.equals(settings.getShortTimeFrame()) && !timeFrame.equals(settings.getLongTimeFrame())) {
                continue;
            }

            boolean shortStrategy = timeFrame.equals(settings.getShortTimeFrame())
                    && rsiDto.getRsi().compareTo(settings.getShortRsi()) >= 0;
            boolean longStrategy = timeFrame.equals(settings.getLongTimeFrame())
                    && rsiDto.getRsi().compareTo(settings.getLongRsi()) <= 0;

            if (!shortStrategy && !longStrategy) {
                continue;
            }

            final String key = rsiDto.getSymbol() + '|' + telegramId + '|' + timeFrame;
            final int strategyCount = strategyCounts
                    .computeIfAbsent(key, k -> {
                        LongAdder adder = new LongAdder();
                        adder.increment();
                        return adder;
                    })
                    .intValue();

            final NotificationDto notificationDto = new NotificationDto()
                    .setTelegramId(telegramId)
                    .setStrategy(Strategy.RSI)
                    .setSymbol(rsiDto.getSymbol())
                    .setTimeframe(timeFrame)
                    .setDirection(shortStrategy ? Direction.DOWN : Direction.UP)
                    .setRsi(rsiDto.getRsi())
                    .setSignalNumber(strategyCount);

            notificationPublisher.sendMessage(notificationDto);
            strategyCounts.get(key).increment();
        }
    }

    public int resetDailyStrategyCounts() {
        int before = strategyCounts.size();
        strategyCounts.clear();
        return before;
    }
}
