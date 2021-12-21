package com.ringcentral.platform.metrics.x.histogram.hdr.configs;

import com.github.rollingmetrics.histogram.OverflowResolver;
import com.github.rollingmetrics.histogram.hdr.RecorderSettings;
import com.ringcentral.platform.metrics.x.histogram.configs.XHistogramImplConfigBuilder;
import com.ringcentral.platform.metrics.x.histogram.hdr.HdrXHistogramImpl;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static com.ringcentral.platform.metrics.x.histogram.hdr.configs.HdrXHistogramImplConfig.*;

public class HdrXHistogramImplConfigBuilder implements XHistogramImplConfigBuilder<HdrXHistogramImplConfig> {

    private HdrXHistogramImpl.Type type = DEFAULT_TYPE;
    private int chunkCount;
    private long chunkResetPeriodMs;
    private RecorderSettings recorderSettings = DEFAULT.recorderSettings();
    private Duration snapshotTtl;

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
        this.type = HdrXHistogramImpl.Type.NEVER_RESET;
        return this;
    }

    public HdrXHistogramImplConfigBuilder resetOnSnapshot() {
        this.type = HdrXHistogramImpl.Type.RESET_ON_SNAPSHOT;
        return this;
    }

    public HdrXHistogramImplConfigBuilder resetPeriodically(Duration period) {
        this.type = HdrXHistogramImpl.Type.RESET_BY_CHUNKS;
        return resetByChunks(0, period.toMillis());
    }

    private HdrXHistogramImplConfigBuilder resetByChunks(int chunkCount, long chunkResetPeriodMs) {
        checkArgument(chunkCount >= 0, "chunkCount must be >= 0");
        checkArgument(chunkCount <= MAX_CHUNKS, "chunkCount must be <= " + MAX_CHUNKS);
        this.chunkCount = chunkCount;

        checkArgument(
            chunkResetPeriodMs >= MIN_CHUNK_RESET_PERIOD_MS,
            "chunkResetPeriodMs must be >= " + MIN_CHUNK_RESET_PERIOD_MS + " ms");

        this.chunkResetPeriodMs = chunkResetPeriodMs;
        return this;
    }

    public HdrXHistogramImplConfigBuilder resetByChunks(int chunkCount, Duration allChunksResetPeriod) {
        checkArgument(chunkCount >= 2, "chunkCount must be >= 2");
        return resetByChunks(chunkCount, allChunksResetPeriod.toMillis() / chunkCount);
    }

    public HdrXHistogramImplConfigBuilder significantDigits(int significantDigits) {
        this.recorderSettings = recorderSettings.withSignificantDigits(significantDigits);
        return this;
    }

    public HdrXHistogramImplConfigBuilder lowestDiscernibleValue(long lowestDiscernibleValue) {
        this.recorderSettings = recorderSettings.withLowestDiscernibleValue(lowestDiscernibleValue);
        return this;
    }

    public HdrXHistogramImplConfigBuilder highestTrackableValue(long highestTrackableValue, OverflowBehavior overflowBehavior) {
        OverflowResolver overflowResolver =
            overflowBehavior == OverflowBehavior.REDUCE_TO_HIGHEST_TRACKABLE ?
            OverflowResolver.REDUCE_TO_HIGHEST_TRACKABLE :
            OverflowResolver.SKIP;

        this.recorderSettings = recorderSettings.withHighestTrackableValue(
            highestTrackableValue,
            overflowResolver);

        return this;
    }

    public HdrXHistogramImplConfigBuilder expectedIntervalBetweenValueSamples(long expectedIntervalBetweenValueSamples) {
        this.recorderSettings = recorderSettings.withExpectedIntervalBetweenValueSamples(expectedIntervalBetweenValueSamples);
        return this;
    }

    public HdrXHistogramImplConfigBuilder noSnapshotOptimization() {
        this.recorderSettings = recorderSettings.withoutSnapshotOptimization();
        return this;
    }

    public HdrXHistogramImplConfigBuilder snapshotTtl(long ttl, ChronoUnit ttlUnit) {
        checkArgument(ttl > 0L, "ttl <= 0");
        this.snapshotTtl = Duration.of(ttl, ttlUnit);
        return this;
    }

    public HdrXHistogramImplConfig build() {
        recorderSettings.validateParameters();

        return new HdrXHistogramImplConfig(
            type,
            chunkCount,
            chunkResetPeriodMs,
            recorderSettings,
            snapshotTtl);
    }
}
