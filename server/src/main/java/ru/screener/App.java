package ru.screener;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;
import ru.screener.config.cache.RsiSettingsLoaderProperties;
import ru.screener.config.kafka.KafkaTopicProperties;

@SpringBootApplication
@EnableKafka
@EnableConfigurationProperties({ RsiSettingsLoaderProperties.class, KafkaTopicProperties.class })
public class App {
    public static void main(String[] args) {
        new SpringApplicationBuilder(App.class)
                .bannerMode(Banner.Mode.OFF)
                .run(args);
    }
}
