package ru.screener.model.kafka.bybit.strategies;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RsiEvent {

    @JsonProperty("Symbol")
    private String symbol;
    @JsonProperty("TimeFrame")
    private String timeframe;
    @JsonProperty("RSI")
    private BigDecimal rsi;
}
