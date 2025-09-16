package ru.screener.dto.bybit.notification;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.screener.model.bybit.strategies.Direction;
import ru.screener.model.bybit.strategies.Strategy;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class NotificationDto {

    private Long telegramId;
    private Strategy strategy;
    private String symbol;
    private String timeframe;
    private Direction direction;
    private BigDecimal rsi;
    private int signalNumber;
}
