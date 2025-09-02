package ru.screener.model.strategy;

import ru.screener.model.market.MarketData;

public interface Strategy {

    boolean evaluate(MarketData marketData);
    String getDescription();
}
