package com.ringcentral.platform.metrics.x.histogram.hdr.configs;

import com.ringcentral.platform.metrics.x.histogram.configs.AbstractXHistogramImplConfigBuilder;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static com.ringcentral.platform.metrics.utils.Preconditions.*;
import static com.ringcentral.platform.metrics.x.histogram.hdr.configs.HdrXHistogramImplConfig.*;

@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "FieldMayBeFinal" })
public class HdrXHistogramImplConfigBuilder extends AbstractXHistogramImplConfigBuilder<HdrXHistogramImplConfig, HdrXHistogramImplConfigBuilder> {

    private HdrXHistogramImplType type = DEFAULT.type();
    private int chunkCount = DEFAULT.chunkCount();
    private long chunkResetPeriodMs = DEFAULT.chunkResetPeriodMs();
    private int significantDigitCount = DEFAULT.significantDigitCount();
    private Optional<Long> lowestDiscernibleValue = DEFAULT.lowestDiscernibleValue();
    private Optional<Long> highestTrackableValue = DEFAULT.highestTrackableValue();
    private Optional<OverflowBehavior> overflowBehavior = DEFAULT.overflowBehavior();
    private Optional<Long> expectedUpdateInterval = DEFAULT.expectedUpdateInterval();

    public static HdrXHistogramImplConfigBuilder hdr() {
        return hdrXHistogramImplConfigBuilder();
    }

    public static HdrXHistogramImplConfigBuilder hdrImpl() {
        return hdrXHistogramImplConfigBuilder();
    }

    public static HdrXHistogramImplConfigBuilder hdrXImpl() {
        return hdrXHistogramImplConfigBuilder();
    }

    public static HdrXHistogramImplConfigBuilder hdrXHistogramImplConfigBuilder() {
        return new HdrXHistogramImplConfigBuilder();
    }

    public HdrXHistogramImplConfigBuilder neverReset() {
        this.type = HdrXHistogramImplType.NEVER_RESET;
        return this;
    }

    public HdrXHistogramImplConfigBuilder resetOnSnapshot() {
        this.type = HdrXHistogramImplType.RESET_ON_SNAPSHOT;
        return this;
    }

    public HdrXHistogramImplConfigBuilder resetPeriodically(Duration period) {
        this.type = HdrXHistogramImplType.RESET_BY_CHUNKS;
        return resetByChunks(0, period.toMillis());
    }

    private HdrXHistogramImplConfigBuilder resetByChunks(int chunkCount, long chunkResetPeriodMs) {
        checkArgument(chunkCount >= 0, "chunkCount must be >= 0");
        checkArgument(chunkCount <= MAX_CHUNKS, "chunkCount must be <= " + MAX_CHUNKS);

        checkArgument(
            chunkResetPeriodMs >= MIN_CHUNK_RESET_PERIOD_MS,
            "chunkResetPeriodMs must be >= " + MIN_CHUNK_RESET_PERIOD_MS + " ms");

        this.chunkCount = chunkCount;
        this.chunkResetPeriodMs = chunkResetPeriodMs;
        return this;
    }

    public HdrXHistogramImplConfigBuilder resetByChunks() {
        return resetByChunks(DEFAULT_CHUNKS, DEFAULT_CHUNKS * DEFAULT_CHUNK_RESET_PERIOD_MS);
    }

    public HdrXHistogramImplConfigBuilder resetByChunks(int chunkCount, Duration allChunksResetPeriod) {
        checkArgument(chunkCount >= 2, "chunkCount must be >= 2");
        return resetByChunks(chunkCount, allChunksResetPeriod.toMillis() / chunkCount);
    }

    public HdrXHistogramImplConfigBuilder significantDigits(int significantDigitCount) {
        checkArgument(
            significantDigitCount >= MIN_SIGNIFICANT_DIGITS && significantDigitCount <= MAX_SIGNIFICANT_DIGITS,
            "significantDigitCount must be between " + MIN_SIGNIFICANT_DIGITS + " and " + MAX_SIGNIFICANT_DIGITS);

        this.significantDigitCount = significantDigitCount;
        return this;
    }

    public HdrXHistogramImplConfigBuilder lowestDiscernibleValue(long lowestDiscernibleValue) {
        checkArgument(
            lowestDiscernibleValue >= MIN_LOWEST_DISCERNIBLE_VALUE,
            "lowestDiscernibleValue must be >= " + MIN_LOWEST_DISCERNIBLE_VALUE);

        this.lowestDiscernibleValue = Optional.of(lowestDiscernibleValue);
        return this;
    }

    public HdrXHistogramImplConfigBuilder highestTrackableValue(long highestTrackableValue, OverflowBehavior overflowBehavior) {
        checkArgument(
            highestTrackableValue >= MIN_HIGHEST_TRACKABLE_VALUE,
            "highestTrackableValue must be >= " + MIN_HIGHEST_TRACKABLE_VALUE);

        this.highestTrackableValue = Optional.of(highestTrackableValue);
        this.overflowBehavior = Optional.of(overflowBehavior);
        return this;
    }

    public HdrXHistogramImplConfigBuilder expectedUpdateInterval(long expectedUpdateInterval) {
        this.expectedUpdateInterval = Optional.of(expectedUpdateInterval);
        return this;
    }

    public HdrXHistogramImplConfigBuilder snapshotTtl(long ttl, ChronoUnit ttlUnit) {
        checkArgument(ttl > 0L, "ttl must be positive");
        this.snapshotTtl = Optional.of(Duration.of(ttl, ttlUnit));
        return this;
    }

    public HdrXHistogramImplConfig build() {
        validate();

        return new HdrXHistogramImplConfig(
            type,
            chunkCount,
            chunkResetPeriodMs,
            significantDigitCount,
            lowestDiscernibleValue,
            highestTrackableValue,
            overflowBehavior,
            expectedUpdateInterval,
            totalsMeasurementType,
            bucketsMeasurementType,
            snapshotTtl);
    }

    private void validate() {
        if (lowestDiscernibleValue.isPresent()) {
            checkState(
                highestTrackableValue.isPresent(),
                "highestTrackableValue must be configured if lowestDiscernibleValue is");

            checkState(
                highestTrackableValue.get() >= 2L * lowestDiscernibleValue.get(),
                "highestTrackableValue must be >= 2 * lowestDiscernibleValue");
        }
    }
}
