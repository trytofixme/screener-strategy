package ru.screener.integrations.response;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.screener.model.strategy.StrategyConfig;

import java.util.List;
import java.util.Locale;

@Data
@Accessors(chain = true)
public class UserResponse {

    private String telegramId;
    private String username;
    private String phoneNumber;
    private Locale locale;
    private List<StrategyConfig> strategyConfigs;
}
