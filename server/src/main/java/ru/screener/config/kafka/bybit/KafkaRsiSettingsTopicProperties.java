package ru.screener.config.kafka.bybit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.kafka.topic.user")
@Configuration
public class KafkaRsiSettingsTopicProperties {

    private String name;
    private int partitions;
}
