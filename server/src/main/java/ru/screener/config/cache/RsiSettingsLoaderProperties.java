package ru.screener.config.cache;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.rsi.settings-loader")
public class RsiSettingsLoaderProperties {

    private Duration timeout = Duration.ofSeconds(10);
    private int retryCount = 3;
    private Duration retryBackoff = Duration.ofSeconds(1);
    private double jitterFactor = 0.3;
}
