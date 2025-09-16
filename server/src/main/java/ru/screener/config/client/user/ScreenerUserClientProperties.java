package ru.screener.config.client.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "services.screener-user")
public class ScreenerUserClientProperties {

    private String baseUrl;
    private String rsiSettingsUri;
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration responseTimeout = Duration.ofSeconds(12);
    private int readTimeout = 12;
    private int writeTimeout = 5;

    private int retryCount = 3;
    private Duration retryBackoff = Duration.ofSeconds(1);
    private Duration retryMaxBackoff = Duration.ofSeconds(8);
}
