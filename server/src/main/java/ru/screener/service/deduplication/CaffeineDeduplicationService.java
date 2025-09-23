package ru.screener.service.deduplication;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
public class CaffeineDeduplicationService {

    private final Cache<Long, Boolean> messagesCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofDays(1))
            .maximumSize(100_000)
            .build();

    public Boolean firstTime(Long crc32) {
        if (crc32 == null) {
            return true;
        }

        return messagesCache.asMap().putIfAbsent(crc32, Boolean.TRUE) == null;
    }
}
