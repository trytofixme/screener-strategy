package ru.screener.config.client.user;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableConfigurationProperties(ScreenerUserClientProperties.class)
public class ScreenerUserClientConfig {

    @Bean
    public WebClient userServiceWebClient(WebClient.Builder builder,
                                          ScreenerUserClientProperties clientProperties,
                                          ExchangeFilterFunction errorMappingFilter) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) clientProperties.getConnectTimeout().toMillis())
                .responseTimeout(clientProperties.getResponseTimeout())
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(clientProperties.getReadTimeout()))
                        .addHandlerLast(new WriteTimeoutHandler(clientProperties.getWriteTimeout())));

        return builder
                .baseUrl(clientProperties.getBaseUrl())
                .filter(errorMappingFilter)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
