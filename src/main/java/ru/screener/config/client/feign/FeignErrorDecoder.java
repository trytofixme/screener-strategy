package ru.screener.config.client.feign;

import feign.Request;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import ru.screener.errors.client.RemoteNotFoundException;
import ru.screener.errors.user.UserUnauthorizedException;

import java.util.Objects;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (Objects.equals(response.status(), HttpStatus.NOT_FOUND.value())) {
            String errorMsg = "Remote resource not found: " + extractRequestUrl(response);
            log.error(errorMsg);
            return new RemoteNotFoundException(errorMsg);
        } else if (Objects.equals(response.status(), HttpStatus.UNAUTHORIZED.value())) {
            String errorMsg = "User ID is missing. Please make sure 'x-user-id' header is included in the request.";
            log.error(errorMsg);
            throw new UserUnauthorizedException(errorMsg);
        }
        else {
            log.error("Remote resource error: {}", methodKey);
            return defaultDecoder.decode(methodKey, response);
        }
    }

    private String extractRequestUrl(Response response) {
        Request request = response.request();
        return request.httpMethod().name() + " " + request.url();
    }
}
