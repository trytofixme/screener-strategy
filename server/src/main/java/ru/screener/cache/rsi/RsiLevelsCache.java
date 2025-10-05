package ru.screener.cache.rsi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.screener.model.bybit.settings.RsiLevelState;
import ru.screener.model.bybit.strategies.Direction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class RsiLevelsCache {

    private final ConcurrentMap<String, RsiLevelState> levelStates = new ConcurrentHashMap<>();

    public BigDecimal getLastReportedValue(String baseKey, boolean isLong) {
        final String key = getCacheKey(baseKey, isLong);
        final RsiLevelState levelState = levelStates.get(key);
        return levelState != null ? levelState.getLastReportedValue() : null;
    }

    public boolean checkAndUpdateLevel(String baseKey,
                                       BigDecimal rsi,
                                       BigDecimal threshold,
                                       BigDecimal step,
                                       boolean isLong) {

        final String cacheKey = getCacheKey(baseKey, isLong);

        final RsiLevelState before = levelStates.get(cacheKey);
        final BigDecimal beforeReported = before != null ? before.getLastReportedValue() : null;

        final RsiLevelState levelState = levelStates.compute(cacheKey, (ignore, previousState) -> {
            if (previousState == null) {
                RsiLevelState initState = new RsiLevelState()
                        .setLastLevel(null)
                        .setLastSeen(rsi)
                        .setMaxSinceSignal(rsi)
                        .setMinSinceSignal(rsi)
                        .setDirection(Direction.NONE)
                        .setLastReportedValue(null);

                Integer firstBucket = isLong ? bucketLong(rsi, threshold, step) : bucketShort(rsi, threshold, step);
                if (firstBucket != null) {
                    BigDecimal level = isLong ? bucketToLevelLong(firstBucket, threshold, step)
                            : bucketToLevelShort(firstBucket, threshold, step);
                    return new RsiLevelState()
                            .setLastLevel(firstBucket)
                            .setLastSeen(rsi)
                            .setMaxSinceSignal(rsi)
                            .setMinSinceSignal(rsi)
                            .setDirection(Direction.NONE)
                            .setLastReportedValue(level);
                }
                return initState;
            }

            int compareResult = rsi.compareTo(previousState.getLastSeen());
            Direction dir = (compareResult > 0 ? Direction.UP : (compareResult < 0 ? Direction.DOWN : previousState.getDirection()));

            BigDecimal maxSince = previousState.getMaxSinceSignal().max(rsi);
            BigDecimal minSince = previousState.getMinSinceSignal().min(rsi);

            Integer prevBucket = previousState.getLastLevel();
            Integer currBucket = isLong ? bucketLong(rsi, threshold, step) : bucketShort(rsi, threshold, step);

            RsiLevelState base = new RsiLevelState()
                    .setLastLevel(previousState.getLastLevel())
                    .setLastSeen(rsi)
                    .setMaxSinceSignal(maxSince)
                    .setMinSinceSignal(minSince)
                    .setDirection(dir)
                    .setLastReportedValue(previousState.getLastReportedValue());

            boolean leavingArea = (currBucket == null);

            if (!Objects.equals(prevBucket, currBucket) && !leavingArea) {
                boolean advancing = (prevBucket == null) || (currBucket > prevBucket);
                if (advancing) {
                    boolean inSide = isLong ? (rsi.compareTo(threshold) <= 0)
                            : (rsi.compareTo(threshold) >= 0);
                    if (prevBucket == null && inSide) {
                        BigDecimal delta = (dir == Direction.DOWN)
                                ? maxSince.subtract(rsi)
                                : rsi.subtract(minSince);
                        if (dir != Direction.NONE && delta.compareTo(step) >= 0) {
                            return new RsiLevelState()
                                    .setLastLevel(previousState.getLastLevel())
                                    .setLastSeen(rsi)
                                    .setMaxSinceSignal(rsi)
                                    .setMinSinceSignal(rsi)
                                    .setDirection(Direction.NONE)
                                    .setLastReportedValue(rsi);
                        }
                    }

                    BigDecimal level = isLong ? bucketToLevelLong(currBucket, threshold, step)
                            : bucketToLevelShort(currBucket, threshold, step);
                    return new RsiLevelState()
                            .setLastLevel(currBucket)
                            .setLastSeen(rsi)
                            .setMaxSinceSignal(rsi)
                            .setMinSinceSignal(rsi)
                            .setDirection(Direction.NONE)
                            .setLastReportedValue(level);
                }
            }

            boolean inSide = isLong ? (rsi.compareTo(threshold) <= 0)
                            : (rsi.compareTo(threshold) >= 0);

            if (dir != Direction.NONE && inSide) {
                BigDecimal delta = (dir == Direction.DOWN)
                        ? maxSince.subtract(rsi)
                        : rsi.subtract(minSince);

                if (delta.compareTo(step) >= 0) {
                    return new RsiLevelState()
                            .setLastLevel(previousState.getLastLevel())
                            .setLastSeen(rsi)
                            .setMaxSinceSignal(rsi)
                            .setMinSinceSignal(rsi)
                            .setDirection(Direction.NONE)
                            .setLastReportedValue(rsi);
                }
            }

            if (leavingArea) {
                base.setLastLevel(null);
            }
            return base;
        });

        return levelState != null
                && levelState.getLastReportedValue() != null
                && !Objects.equals(levelState.getLastReportedValue(), beforeReported);
    }

    private String getCacheKey(String baseKey, boolean isLong) {
        return baseKey + "|" + (isLong ? "LONG" : "SHORT");
    }

    private Integer bucketShort(BigDecimal rsi, BigDecimal threshold, BigDecimal step) {
        BigDecimal start = threshold.add(step);
        if (rsi.compareTo(start) < 0) {
            return null;
        }

        BigDecimal bucket = rsi.subtract(start).divide(step, 0, RoundingMode.FLOOR);
        return bucket.intValueExact();
    }

    private Integer bucketLong(BigDecimal rsi, BigDecimal threshold, BigDecimal step) {
        BigDecimal start = threshold.subtract(step);
        if (rsi.compareTo(start) > 0) {
            return null;
        }

        BigDecimal bucket = start.subtract(rsi).divide(step, 0, RoundingMode.FLOOR);
        return bucket.intValueExact();
    }

    public BigDecimal bucketToLevelShort(int bucket, BigDecimal threshold, BigDecimal step) {
        BigDecimal start = threshold.add(step);
        return start.add(step.multiply(BigDecimal.valueOf(bucket)));
    }

    public BigDecimal bucketToLevelLong(int bucket, BigDecimal threshold, BigDecimal step) {
        BigDecimal start = threshold.subtract(step);
        return start.subtract(step.multiply(BigDecimal.valueOf(bucket)));
    }
}
