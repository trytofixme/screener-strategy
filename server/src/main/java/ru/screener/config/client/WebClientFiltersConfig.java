package ru.screener.config.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.screener.errors.ExternalClientException;

import java.util.function.Predicate;

@Configuration
public class WebClientFiltersConfig {

    private static final int TOO_MANY_REQUESTS_EXCEPTION_CODE = 429;
    private static final int SERVER_EXCEPTION_CODE = 500;

    @Bean
    public ExchangeFilterFunction errorMappingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().is5xxServerError()
                    || clientResponse.statusCode().value() == TOO_MANY_REQUESTS_EXCEPTION_CODE) {
                return clientResponse.createException()
                        .flatMap(Mono::error);
            }
            if (clientResponse.statusCode().is4xxClientError()) {
                return clientResponse.createException()
                        .flatMap(ex -> Mono.error(new ExternalClientException(ex)));
            }
            return Mono.just(clientResponse);
        });
    }

    @Bean
    public Predicate<Throwable> retryablePredicate() {
        return ex -> {
            if (ex instanceof WebClientResponseException w) {
                int code = w.getStatusCode().value();
                return code >= SERVER_EXCEPTION_CODE || code == TOO_MANY_REQUESTS_EXCEPTION_CODE;
            }
            return !(ex instanceof ExternalClientException);
        };
    }
}
