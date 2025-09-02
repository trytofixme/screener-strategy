package ru.screener.integrations.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import ru.screener.model.kafka.bybit.settings.RsiSettingsEvent;

import java.util.List;

@FeignClient(
        name = "user-service",
        url = "${services.user-service.host}:${services.user-service.port}"
)
public interface UserSettingsClient {

    @GetMapping("/settings/rsi")
    List<RsiSettingsEvent> getRsiSettings();
}

