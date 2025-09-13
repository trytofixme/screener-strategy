package ru.screener.integrations.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;
import ru.screener.model.kafka.bybit.settings.RsiSettingsEvent;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSettingsClient {

    private final WebClient userServiceWebClient;

    public Flux<RsiSettingsEvent> getRsiSettings() {
        return userServiceWebClient
                .get()
                .uri("/api/v1/settings/rsi")
                .retrieve()
                .bodyToFlux(RsiSettingsEvent.class)
                .timeout(Duration.ofSeconds(5))
                .retryWhen(Retry
                        .backoff(4, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofSeconds(8))
                        .filter(this::isRetryable))
                .doOnSubscribe(s -> log.info("Fetching RSI settings..."))
                .doOnError(e -> log.error("Failed to fetch RSI settings", e));
    }

    private boolean isRetryable(Throwable t) {
        if (t instanceof WebClientResponseException ex) {
            int code = ex.getStatusCode().value();
            return code >= 500 || code == 429;
        }

        return true;
    }
}
