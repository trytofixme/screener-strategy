package ru.screener.cache.rsi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RsiLevelsCacheTest {

    private static final String CACHE_KEY = "DBRUSDT|15m";
    private static final BigDecimal SHORT_VALUE = bd(80);
    private static final BigDecimal LONG_VALUE = bd(20);
    private static final BigDecimal DUMP_VALUE = bd(2);

    private final RsiLevelsCache cache = new RsiLevelsCache();

    @Test
    @DisplayName("SHORT: 82, 84, 86, 88 по сетке и 86.2 по дрейфу от экстремума 88.2")
    void testShortStrategy_withDrift() {
        List<BigDecimal> data = List.of(
                bd(78.0), bd(82.0), bd(83.0), bd(83.5), bd(84.0), bd(84.2), bd(84.5), bd(84.8),
                bd(85.0), bd(86.0), bd(86.5), bd(88.0), bd(88.2), bd(87.5), bd(86.2), bd(85.4),
                bd(84.1), bd(86.4), bd(85.5), bd(84.4), bd(82.2), bd(81.5), bd(80.2), bd(80.0),
                bd(79.0), bd(78.0)
        );

        List<BigDecimal> notified = new ArrayList<>();

        for (BigDecimal rsi : data) {
            boolean notify = cache.checkAndUpdateLevel(CACHE_KEY, rsi, SHORT_VALUE, DUMP_VALUE, false);
            if (notify) {
                notified.add(cache.getLastReportedValue(CACHE_KEY, false));
            }
        }

        List<BigDecimal> expected = List.of(
                bd(82.0), bd(84.0), bd(86.0), bd(88.0), bd(86.2),
                bd(84.1), bd(86.4), bd(84.4), bd(82.2), bd(80.2)
        );
        assertEquals(expected, notified);
    }

    @Test
    @DisplayName("LONG: 18, 16, 14 по сетке и дрейфы 16.2, 19.9, 17.8 (от локальных минимумов/максимумов)")
    void testLongStrategy_withDrift() {
        List<BigDecimal> data = List.of(bd(22.0), bd(18.0), bd(17.0), bd(16.0), bd(15.8), bd(14.0),
                bd(13.8), bd(14.5), bd(16.2), bd(17.1), bd(18.1), bd(19.9), bd(20.5), bd(19.0),
                bd(17.8), bd(18.2), bd(20.2)
        );

        List<BigDecimal> notified = new ArrayList<>();

        for (BigDecimal rsi : data) {
            boolean notify = cache.checkAndUpdateLevel(CACHE_KEY, rsi, LONG_VALUE, DUMP_VALUE, true);
            if (notify) {
                notified.add(cache.getLastReportedValue(CACHE_KEY, true));
            }
        }

        List<BigDecimal> expected = List.of(bd(18.0), bd(16.0), bd(14.0), bd(16.2), bd(19.9), bd(17.8));
        assertEquals(expected, notified);
    }


    private static BigDecimal bd(double v) { return BigDecimal.valueOf(v); }
}
