package ru.screener.model.kafka.bybit.notification;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.screener.model.strategies.Direction;
import ru.screener.model.strategies.Strategy;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class NotificationEvent {

    private Long telegramId;
    private Strategy strategy;
    private String symbol;
    private String timeframe;
    private Direction direction;
    private BigDecimal rsi;
    private int signalNumber;
}
