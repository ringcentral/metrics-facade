package com.ringcentral.platform.metrics.x.histogram.hdr.configs;

import com.ringcentral.platform.metrics.x.histogram.configs.*;

import java.time.Duration;
import java.util.Optional;

import static com.ringcentral.platform.metrics.utils.TimeUnitUtils.MS_PER_SEC;
import static java.util.Objects.requireNonNull;

@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "ConstantConditions" })
public class HdrXHistogramImplConfig extends AbstractXHistogramImplConfig {

    public static final HdrXHistogramImplType DEFAULT_TYPE = HdrXHistogramImplType.RESET_BY_CHUNKS;

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
        DEFAULT_TOTALS_MEASUREMENT_TYPE,
        DEFAULT_BUCKETS_MEASUREMENT_TYPE,
        Optional.ofNullable(DEFAULT_SNAPSHOT_TTL));

    private final HdrXHistogramImplType type;
    private final int chunkCount;
    private final long chunkResetPeriodMs;
    private final int significantDigitCount;
    private final Optional<Long> lowestDiscernibleValue;
    private final Optional<Long> highestTrackableValue;
    private final Optional<OverflowBehavior> overflowBehavior;
    private final Optional<Long> expectedUpdateInterval;

    public HdrXHistogramImplConfig(
        HdrXHistogramImplType type,
        int chunkCount,
        long chunkResetPeriodMs,
        int significantDigitCount,
        Optional<Long> lowestDiscernibleValue,
        Optional<Long> highestTrackableValue,
        Optional<OverflowBehavior> overflowBehavior,
        Optional<Long> expectedUpdateInterval,
        TotalsMeasurementType totalsMeasurementType,
        BucketsMeasurementType bucketsMeasurementType,
        Optional<Duration> snapshotTtl) {

        super(
            totalsMeasurementType,
            bucketsMeasurementType,
            snapshotTtl);

        this.type = type;
        this.chunkCount = chunkCount;
        this.chunkResetPeriodMs = chunkResetPeriodMs;
        this.significantDigitCount = significantDigitCount;
        this.lowestDiscernibleValue = requireNonNull(lowestDiscernibleValue);
        this.highestTrackableValue = requireNonNull(highestTrackableValue);
        this.overflowBehavior = requireNonNull(overflowBehavior);
        this.expectedUpdateInterval = requireNonNull(expectedUpdateInterval);
    }

    public HdrXHistogramImplType type() {
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
}
