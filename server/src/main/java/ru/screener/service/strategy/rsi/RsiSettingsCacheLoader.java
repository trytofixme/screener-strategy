package ru.screener.service.strategy.rsi;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.screener.integrations.user.UserSettingsClient;

@Component
@RequiredArgsConstructor
public class RsiSettingsCacheLoader {

    private final UserSettingsClient userSettingsClient;
    private final RsiProcessor rsiProcessor;

    @PostConstruct
    public void load() {
        rsiProcessor.loadInitialSettings(userSettingsClient.getRsiSettings());
    }
}

