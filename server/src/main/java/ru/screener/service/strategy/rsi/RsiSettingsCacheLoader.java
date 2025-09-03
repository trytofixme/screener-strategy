package ru.screener.service.strategy.rsi;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.util.retry.Retry;
import ru.screener.integrations.user.UserSettingsClient;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class RsiSettingsCacheLoader implements ApplicationListener<ApplicationReadyEvent> {

    private final UserSettingsClient userSettingsClient;
    private final RsiProcessor rsiProcessor;

    private Disposable subscription;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        subscription = rsiProcessor.loadInitialSettings(
                        userSettingsClient.getRsiSettings()
                                .doOnNext(settings -> log.info("Got settings: {}", settings))
                                .timeout(Duration.ofSeconds(10))
                                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                                .doOnSubscribe(s -> log.info("Loading initial RSI settings..."))
                                .doOnError(e -> log.error("Failed to load RSI settings", e))
                                .doOnComplete(() -> log.info("Initial RSI settings loaded"))
                )
                .subscribe(
                        null,
                        e -> log.error("Unexpected error after load pipeline", e),
                        () -> log.info("Initial load pipeline completed")
                );
    }

    @PreDestroy
    public void stop() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
    }
}
