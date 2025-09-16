package ru.screener.cache.rsi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.screener.dto.bybit.settings.RsiSettingsDto;
import ru.screener.mapper.bybit.settings.RsiSettingsMapper;
import ru.screener.model.bybit.settings.RsiSettings;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RsiSettingsCache {

    private final RsiSettingsMapper rsiSettingsMapper;
    private final Map<Long, RsiSettings> rsiSettingsByTgUserId = new ConcurrentHashMap<>();

    public Map<Long, RsiSettings> getAllSettings() {
        return Map.copyOf(rsiSettingsByTgUserId);
    }

    public void upsertRsiSettings(RsiSettingsDto rsiSettingsDto) {
        final RsiSettings rsiSettings = rsiSettingsMapper.toDomain(rsiSettingsDto);

        rsiSettingsByTgUserId.put(rsiSettingsDto.getTelegramId(), rsiSettings);
        log.info("userStrategies updated: {}", rsiSettingsByTgUserId);
    }

    public Mono<Void> loadInitial(Flux<RsiSettingsDto> settingsFlux) {
        return settingsFlux
                .map(dto -> Map.entry(dto.getTelegramId(), rsiSettingsMapper.toDomain(dto)))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue, ConcurrentHashMap::new)
                .doOnNext(collected -> {
                    rsiSettingsByTgUserId.clear();
                    rsiSettingsByTgUserId.putAll(collected);
                    log.info("Loaded {} RSI settings on startup", rsiSettingsByTgUserId.size());
                })
                .then();
    }
}
