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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@Slf4j
@Service
@RequiredArgsConstructor
public class RsiProcessor implements StrategyProcessor<RsiDto> {

    private final NotificationPublisher notificationPublisher;
    private final RsiValidator rsiValidator;
    private final RsiSettingsCache rsiSettingsCache;
    private final Map<String, BigDecimal> levels = new ConcurrentHashMap<>();
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

            if (longStrategy) {
                if (!checkAndUpdateLevel(key, rsiDto.getRsi(), settings.getLongRsi(), settings.getLongDump(), true)) {
                    continue;
                }
            }

            if (shortStrategy) {
                if (!checkAndUpdateLevel(key, rsiDto.getRsi(), settings.getShortRsi(), settings.getShortDump(), false)) {
                    continue;
                }
            }

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

    public boolean checkAndUpdateLevel(String baseKey,
                                              BigDecimal rsi,
                                              BigDecimal threshold,
                                              BigDecimal step,
                                              boolean isLong) {
        final String side = isLong ? "LONG" : "SHORT";
        final String key = baseKey + "|" + side;

        final BigDecimal prev = levels.get(key);
        final BigDecimal current = isLong
                ? snapLong(rsi, threshold, step)
                : snapShort(rsi, threshold, step);

        if (current == null) {
            levels.remove(key);
            return false;
        }

        if (isLong && prev != null && prev.compareTo(threshold) == 0
                && rsi.compareTo(threshold) < 0
                && rsi.compareTo(threshold.subtract(step)) > 0) {
            return false;
        }

        if (!Objects.equals(prev, current)) {
            levels.put(key, current);
            return true;
        }
        return false;
    }

    private BigDecimal snapShort(BigDecimal rsi, BigDecimal threshold, BigDecimal step) {
        if (rsi.compareTo(threshold) < 0) {
            return null;
        }

        BigDecimal diff = rsi.subtract(threshold).divide(step, 0, RoundingMode.FLOOR);
        return threshold.add(step.multiply(diff));
    }

    private BigDecimal snapLong(BigDecimal rsi, BigDecimal threshold, BigDecimal step) {
        if (rsi.compareTo(threshold) > 0) {
            return null;
        }

        BigDecimal diff = threshold.subtract(rsi).divide(step, 0, RoundingMode.CEILING);
        return threshold.subtract(step.multiply(diff));
    }
}
