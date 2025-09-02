package ru.screener.service.strategy;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.screener.config.context.UserContext;
import ru.screener.model.strategy.StrategyConfig;
import ru.screener.integrations.UserServiceClient;
import ru.screener.integrations.response.UserResponse;
import ru.screener.model.market.MarketData;
import ru.screener.dto.notification.NotificationDto;
import ru.screener.model.strategy.Strategy;
import ru.screener.producer.NotificationPublisher;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyProcessor {

    private final UserContext userContext;
    private final UserServiceClient userServiceClient;
    private final StrategyBuilder strategyBuilder;
    private final NotificationPublisher notificationPublisher;
    private final Map<String, List<Strategy>> userStrategies = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        loadAllStrategies();
    }

    public void onQuote(MarketData data) {
        userStrategies.forEach((userId, strategies) -> {
            for (Strategy strategy : strategies) {
                if (strategy.evaluate(data)) {
                    NotificationDto notification = new NotificationDto(userId, strategy.getDescription());
                    notificationPublisher.sendMessage(notification);
                }
            }
        });
    }

    public void loadAllStrategies() {
        UserResponse response = userServiceClient.getUserInfo(userContext.getUserId());
        List<StrategyConfig> configs = response.getStrategyConfigs();
        List<Strategy> strategies = strategyBuilder.buildFrom(configs);

        userStrategies.put(response.getTelegramId(), strategies);
    }
}
