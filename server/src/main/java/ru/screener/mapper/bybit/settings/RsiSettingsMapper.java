package ru.screener.mapper.bybit.settings;

import org.springframework.stereotype.Component;
import ru.screener.model.bybit.settings.RsiSettings;
import ru.screener.dto.bybit.settings.RsiSettingsDto;

@Component
public class RsiSettingsMapper {

    public RsiSettings toDomain(RsiSettingsDto rsiSettingsEvent) {
        return new RsiSettings()
                .setShortTimeFrame(rsiSettingsEvent.getShortTimeFrame())
                .setLongTimeFrame(rsiSettingsEvent.getLongTimeFrame())
                .setShortRsi(rsiSettingsEvent.getShortRsi())
                .setLongRsi(rsiSettingsEvent.getLongRsi());
    }
}
