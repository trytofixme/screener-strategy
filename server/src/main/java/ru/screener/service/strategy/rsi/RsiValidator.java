package ru.screener.service.strategy.rsi;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;

@Component
public class RsiValidator {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public boolean isRsiSettingsValid(BigDecimal rsiValue) {
        return rsiValue != null && ZERO.compareTo(rsiValue) <= 0 && HUNDRED.compareTo(rsiValue) >= 0;
    }
}
