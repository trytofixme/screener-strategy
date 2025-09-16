package ru.screener.model.bybit.settings;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class RsiSettings {

    private String shortTimeFrame;
    private String longTimeFrame;
    private BigDecimal shortRsi;
    private BigDecimal longRsi;
}
