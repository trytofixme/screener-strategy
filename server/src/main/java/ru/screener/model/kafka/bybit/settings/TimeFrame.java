package ru.screener.model.kafka.bybit.settings;

import lombok.Getter;

@Getter
public enum TimeFrame {

    NONE(""),
    FIFTEEN_MINUTES("15 минут"),
    ONE_HOUR("1 час"),
    FOUR_HOURS("4 часа"),
    ONE_DAY("1 день");

    private final String translation;

    TimeFrame(String translation) {
        this.translation = translation;
    }

    public static TimeFrame fromString(String translation) {
        for (TimeFrame timeFrame : values()) {
            if (timeFrame.translation.equalsIgnoreCase(translation)) {
                return timeFrame;
            }
        }

        throw new IllegalArgumentException("Выберите значение из меню");
    }
}

