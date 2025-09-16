package ru.screener.config.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@EnableConfigurationProperties(StrategyCountsSchedulerProperties.class)
public class SchedulingConfig {

    private final StrategyCountsSchedulerProperties strategyCountsSchedulerProperties;

    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler strategyCountsScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(strategyCountsSchedulerProperties.getPoolSize());
        scheduler.setThreadNamePrefix(strategyCountsSchedulerProperties.getPrefix());
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.setErrorHandler(ex -> log.error("StrategyCountsScheduler throws error", ex));
        scheduler.initialize();
        return scheduler;
    }
}
