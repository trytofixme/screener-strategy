package ru.screener.mapper.bybit.settings;

import org.springframework.stereotype.Component;
import ru.screener.model.bybit.settings.RsiSettings;
import ru.screener.dto.bybit.settings.RsiSettingsDto;

@Component
public class RsiSettingsMapper {

    public RsiSettings toDomain(RsiSettingsDto rsiSettingsDto) {
        return new RsiSettings()
                .setShortTimeFrame(rsiSettingsDto.getShortTimeFrame())
                .setLongTimeFrame(rsiSettingsDto.getLongTimeFrame())
                .setShortRsi(rsiSettingsDto.getShortRsi())
                .setLongRsi(rsiSettingsDto.getLongRsi())
                .setShortDump(rsiSettingsDto.getShortDump())
                .setLongDump(rsiSettingsDto.getLongDump());
    }
}
