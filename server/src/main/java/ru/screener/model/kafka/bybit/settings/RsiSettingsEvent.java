package ru.screener.model.kafka.bybit.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class RsiSettingsEvent {

    @JsonProperty(value = "telegram_id")
    private String telegramId;

    @JsonProperty(value = "short_time_frame")
    private String shortTimeFrame;
    @JsonProperty(value = "long_time_frame")
    private String longTimeFrame;

    @JsonProperty(value = "short_rsi")
    private BigDecimal shortRsi;
    @JsonProperty(value = "long_rsi")
    private BigDecimal longRsi;
}
