package com.ringcentral.platform.metrics.x.histogram.hdr.configs;

import com.ringcentral.platform.metrics.x.histogram.configs.XHistogramImplConfig;

import java.time.Duration;
import java.util.Optional;

import static com.ringcentral.platform.metrics.utils.TimeUnitUtils.MS_PER_SEC;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class HdrXHistogramImplConfig implements XHistogramImplConfig {

    public static final HdrXHistogramType DEFAULT_TYPE = HdrXHistogramType.RESET_BY_CHUNKS;

    public static final int MAX_CHUNKS = 60;
    public static final long MIN_CHUNK_RESET_PERIOD_MS = MS_PER_SEC;
    public static final int DEFAULT_CHUNKS = 6;
    public static final long DEFAULT_CHUNK_RESET_PERIOD_MS = 20L * MS_PER_SEC;

    public static final int MIN_SIGNIFICANT_DIGITS = 0;
    public static final int MAX_SIGNIFICANT_DIGITS = 5;
    public static final int DEFAULT_SIGNIFICANT_DIGITS = 2;

    public static final int MIN_LOWEST_DISCERNIBLE_VALUE = 1;
    public static final int MIN_HIGHEST_TRACKABLE_VALUE = 2;

    public static final HdrXHistogramImplConfig DEFAULT = new HdrXHistogramImplConfig(
        DEFAULT_TYPE,
        DEFAULT_CHUNKS,
        DEFAULT_CHUNK_RESET_PERIOD_MS,
        DEFAULT_SIGNIFICANT_DIGITS,
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty());

    private final HdrXHistogramType type;
    private final int chunkCount;
    private final long chunkResetPeriodMs;
    private final int significantDigitCount;
    private final Optional<Long> lowestDiscernibleValue;
    private final Optional<Long> highestTrackableValue;
    private final Optional<OverflowBehavior> overflowBehavior;
    private final Optional<Long> expectedUpdateInterval;
    private final Optional<Duration> snapshotTtl;

    public HdrXHistogramImplConfig(
        HdrXHistogramType type,
        int chunkCount,
        long chunkResetPeriodMs,
        int significantDigitCount,
        Optional<Long> lowestDiscernibleValue,
        Optional<Long> highestTrackableValue,
        Optional<OverflowBehavior> overflowBehavior,
        Optional<Long> expectedUpdateInterval,
        Optional<Duration> snapshotTtl) {

        this.type = type;
        this.chunkCount = chunkCount;
        this.chunkResetPeriodMs = chunkResetPeriodMs;
        this.significantDigitCount = significantDigitCount;
        this.lowestDiscernibleValue = requireNonNull(lowestDiscernibleValue);
        this.highestTrackableValue = requireNonNull(highestTrackableValue);
        this.overflowBehavior = requireNonNull(overflowBehavior);
        this.expectedUpdateInterval = requireNonNull(expectedUpdateInterval);
        this.snapshotTtl = requireNonNull(snapshotTtl);
    }

    public HdrXHistogramType type() {
        return type;
    }

    public int chunkCount() {
        return chunkCount;
    }

    public long chunkResetPeriodMs() {
        return chunkResetPeriodMs;
    }

    public int significantDigitCount() {
        return significantDigitCount;
    }

    public Optional<Long> lowestDiscernibleValue() {
        return lowestDiscernibleValue;
    }

    public Optional<Long> highestTrackableValue() {
        return highestTrackableValue;
    }

    public Optional<OverflowBehavior> overflowBehavior() {
        return overflowBehavior;
    }

    public Optional<Long> expectedUpdateInterval() {
        return expectedUpdateInterval;
    }

    public boolean hasSnapshotTtl() {
        return snapshotTtl.isPresent();
    }

    public Optional<Duration> snapshotTtl() {
        return snapshotTtl;
    }
}
