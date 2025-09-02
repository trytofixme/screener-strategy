package ru.screener.service.strategy;

import ru.screener.model.strategy.StrategyConfig;
import ru.screener.model.strategy.Comparison;
import ru.screener.model.strategy.Strategy;
import ru.screener.service.strategy.strategies.RsiStrategy;

import java.util.ArrayList;
import java.util.List;

public class StrategyBuilder {

    public List<Strategy> buildFrom(List<StrategyConfig> strategyConfigs) {
        List<Strategy> strategies = new ArrayList<>();
        strategyConfigs.forEach(config -> {
            StrategyConfig.StrategyDefinition definition = config.getDefinition();
            Comparison comparison = Comparison.valueOf(definition.getCondition());

            switch (definition.getType()) {
                case "RSI" -> strategies.add(
                        new RsiStrategy(definition.getSymbol(), definition.getValue(), comparison)
                );
                // другие стратегии
            }
        });

        return strategies;
    }
}
