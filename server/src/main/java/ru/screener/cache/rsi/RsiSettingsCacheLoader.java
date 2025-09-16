package ru.screener.cache.rsi;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.util.retry.Retry;
import ru.screener.config.cache.RsiSettingsLoaderProperties;
import ru.screener.integrations.user.ScreenerUserClient;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RsiSettingsCacheLoader {

    private final ScreenerUserClient userSettingsClient;
    private final RsiSettingsCache rsiSettingsCache;
    private final RsiSettingsLoaderProperties rsiLoaderProperties;

    private volatile Disposable subscription;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady(ApplicationReadyEvent ignoredEvent) {
        if (subscription != null && !subscription.isDisposed()) {
            log.warn("Initial load already subscribed");
            return;
        }

        subscription = rsiSettingsCache.loadInitial(
                        userSettingsClient.getRsiSettings()
                                .doOnSubscribe(s -> log.info("Loading initial RSI settings..."))
                                .timeout(rsiLoaderProperties.getTimeout())
                                .retryWhen(Retry.backoff(rsiLoaderProperties.getRetryCount(), rsiLoaderProperties.getRetryBackoff())
                                        .jitter(rsiLoaderProperties.getJitterFactor()))
                                .doOnNext(dto -> log.info("Fetched settings: {}", dto))
                                .doOnError(ex -> log.error("Failed to load RSI settings", ex))
                                .doOnComplete(() -> log.info("Initial RSI settings loaded"))
                )
                .subscribe(
                        null,
                        ex -> log.error("Unexpected error after load pipeline", ex),
                        () -> log.info("Initial load pipeline completed")
                );
    }

    @PreDestroy
    public void stop() {
        Optional.ofNullable(subscription)
                .filter(sub -> !sub.isDisposed())
                .ifPresent(Disposable::dispose);
    }
}
