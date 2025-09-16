package ru.screener.integrations.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;
import ru.screener.config.client.user.ScreenerUserClientProperties;
import ru.screener.dto.bybit.settings.RsiSettingsDto;

import java.util.function.Predicate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScreenerUserClient {

    private final WebClient userServiceWebClient;
    private final ScreenerUserClientProperties clientProperties;
    private final Predicate<Throwable> retryablePredicate;

    public Flux<RsiSettingsDto> getRsiSettings() {
        return userServiceWebClient.get()
                .uri(clientProperties.getRsiSettingsUri())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(RsiSettingsDto.class)
                .timeout(clientProperties.getResponseTimeout())
                .retryWhen(Retry.backoff(clientProperties.getRetryCount(), clientProperties.getRetryBackoff())
                        .maxBackoff(clientProperties.getRetryMaxBackoff())
                        .filter(retryablePredicate))
                .doOnSubscribe(subscription -> log.info("Fetching RSI settings..."))
                .doOnError(ex -> log.error("Failed to fetch RSI settings", ex));
    }
}
