package ru.screener.model.kafka.bybit.strategies;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class RsiEvent {

    private String symbol;
    @JsonProperty("timeFrame")
    private String timeframe;
    private BigDecimal rsi;
}
