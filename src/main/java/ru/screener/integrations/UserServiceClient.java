package ru.screener.integrations;

import feign.FeignException;
import feign.RetryableException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.screener.config.client.feign.FeignClientConfig;
import ru.screener.integrations.response.UserResponse;

@FeignClient(name = "screener-user",
        url = "${services.user-service.host}:${services.user-service.port}",
        path = "/api/v1/users",
        configuration = FeignClientConfig.class)
public interface UserServiceClient {

    @Retryable(
            retryFor = { FeignException.class, RetryableException.class },
            maxAttempts = 5,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    @GetMapping("/{telegramId}")
    UserResponse getUserInfo(@PathVariable String telegramId);
}
