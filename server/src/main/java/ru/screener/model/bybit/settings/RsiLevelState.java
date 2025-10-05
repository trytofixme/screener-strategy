package ru.screener.model.bybit.settings;

import jdk.jfr.Description;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.screener.model.bybit.strategies.Direction;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public final class RsiLevelState {

    @Description("Последний уведомлённый bucket (для сетки)")
    Integer lastLevel;

    @Description("Последний уведомлённый bucket (для сетки)")
    BigDecimal lastSeen;

    @Description("Максимум с момента последнего события")
    private BigDecimal maxSinceSignal;

    @Description("Минимум с момента последнего события")
    private BigDecimal minSinceSignal;

    @Description("Текущий тренд с момента последней нотификации")
    Direction direction;

    @Description("Последнее отправленное значение (сетка или дрейф)")
    BigDecimal lastReportedValue;
}
