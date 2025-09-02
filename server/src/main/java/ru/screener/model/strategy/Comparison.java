package ru.screener.model.strategy;

import java.math.BigDecimal;

public enum Comparison {
    LT {
        public boolean compare(BigDecimal a, BigDecimal b) { return a.doubleValue() < b.doubleValue(); }
    },
    GT {
        public boolean compare(BigDecimal a, BigDecimal b) { return a.doubleValue() > b.doubleValue(); }
    };

    public abstract boolean compare(BigDecimal a, BigDecimal b);
}
