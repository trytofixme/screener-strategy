package ru.screener.service.strategy.strategies;

import lombok.RequiredArgsConstructor;
import ru.screener.model.market.MarketData;
import ru.screener.model.strategy.Comparison;
import ru.screener.model.strategy.Strategy;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class RsiStrategy implements Strategy {

    private final String symbol;
    private final BigDecimal threshold;
    private final Comparison comparison;

    @Override
    public boolean evaluate(MarketData data) {
        BigDecimal rsi = data.getRsi();
        return comparison.compare(rsi, threshold);
    }

    @Override
    public String getDescription() {
        return "RSI " + comparison + " " + threshold;
    }
}
