package ru.screener.model.market;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class MarketData {

    private String telegramId;
    private String symbol;
    private String interval;
    private LocalDateTime openTime;
    private double open, high, low, close, volume;
    private long trades;
    private Source source;
    private BigDecimal rsi;
}

