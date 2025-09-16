package ru.screener.config.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.scheduler.strategy-counts")
@Configuration
public class StrategyCountsSchedulerProperties {

    private int poolSize = 2;
    private String prefix = "strategy-counts-";
}
