package ru.screener.model.strategy;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class StrategyConfig {

    private StrategyDefinition definition;
    private int candles;
    private String interval;

    @Data
    @Accessors(chain = true)
    public static class StrategyDefinition {
        private String type;
        private String symbol;
        private BigDecimal value;
        private String condition;
    }
}
