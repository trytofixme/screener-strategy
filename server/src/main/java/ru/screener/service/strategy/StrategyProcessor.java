package ru.screener.service.strategy;

public interface StrategyProcessor<T> {

    void process(T strategyData);
}
