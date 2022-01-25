package com.ringcentral.platform.metrics.x.histogram.scale.configs;

import com.ringcentral.platform.metrics.x.histogram.configs.XHistogramImplConfigBuilder;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static com.ringcentral.platform.metrics.x.histogram.scale.configs.ScaleXHistogramImplConfig.*;
import static java.util.Objects.requireNonNull;

@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "FieldMayBeFinal" })
public class ScaleXHistogramImplConfigBuilder implements XHistogramImplConfigBuilder<ScaleXHistogramImplConfig> {

    private ScaleXHistogramType type = DEFAULT.type();
    private int chunkCount = DEFAULT.chunkCount();
    private long chunkResetPeriodMs = DEFAULT.chunkResetPeriodMs();
    private Scale scale = DEFAULT.scale();
    private boolean bucketsResettable = DEFAULT.areBucketsResettable();
    private Optional<Duration> snapshotTtl = DEFAULT.snapshotTtl();

    public static ScaleXHistogramImplConfigBuilder scale() {
        return scaleXHistogramImplConfigBuilder();
    }

    public static ScaleXHistogramImplConfigBuilder scaleImpl() {
        return scaleXHistogramImplConfigBuilder();
    }

    public static ScaleXHistogramImplConfigBuilder scaleXImpl() {
        return scaleXHistogramImplConfigBuilder();
    }

    public static ScaleXHistogramImplConfigBuilder scaleXHistogramImplConfigBuilder() {
        return new ScaleXHistogramImplConfigBuilder();
    }

    public ScaleXHistogramImplConfigBuilder neverReset() {
        this.type = ScaleXHistogramType.NEVER_RESET;
        return this;
    }

    public ScaleXHistogramImplConfigBuilder resetOnSnapshot() {
        this.type = ScaleXHistogramType.RESET_ON_SNAPSHOT;
        return this;
    }

    public ScaleXHistogramImplConfigBuilder resetPeriodically(Duration period) {
        this.type = ScaleXHistogramType.RESET_BY_CHUNKS;
        return resetByChunks(0, period.toMillis());
    }

    private ScaleXHistogramImplConfigBuilder resetByChunks(int chunkCount, long chunkResetPeriodMs) {
        checkArgument(chunkCount >= 0, "chunkCount must be >= 0");
        checkArgument(chunkCount <= MAX_CHUNKS, "chunkCount must be <= " + MAX_CHUNKS);

        checkArgument(
            chunkResetPeriodMs >= MIN_CHUNK_RESET_PERIOD_MS,
            "chunkResetPeriodMs must be >= " + MIN_CHUNK_RESET_PERIOD_MS + " ms");

        this.chunkCount = chunkCount;
        this.chunkResetPeriodMs = chunkResetPeriodMs;
        return this;
    }

    public ScaleXHistogramImplConfigBuilder resetByChunks(int chunkCount, Duration allChunksResetPeriod) {
        checkArgument(chunkCount >= 2, "chunkCount must be >= 2");
        return resetByChunks(chunkCount, allChunksResetPeriod.toMillis() / chunkCount);
    }

    public ScaleXHistogramImplConfigBuilder with(ScaleBuilder<?> scaleBuilder) {
        return scale(scaleBuilder.build());
    }

    public ScaleXHistogramImplConfigBuilder with(Scale scale) {
        this.scale = requireNonNull(scale);
        return this;
    }

    public ScaleXHistogramImplConfigBuilder scale(ScaleBuilder<?> scaleBuilder) {
        return scale(scaleBuilder.build());
    }

    public ScaleXHistogramImplConfigBuilder scale(Scale scale) {
        this.scale = requireNonNull(scale);
        return this;
    }

    public ScaleXHistogramImplConfigBuilder bucketsResettable(boolean bucketsResettable) {
        this.bucketsResettable = bucketsResettable;
        return this;
    }

    public ScaleXHistogramImplConfigBuilder snapshotTtl(long ttl, ChronoUnit ttlUnit) {
        checkArgument(ttl > 0L, "ttl must be positive");
        this.snapshotTtl = Optional.of(Duration.of(ttl, ttlUnit));
        return this;
    }

    public ScaleXHistogramImplConfig build() {
        return new ScaleXHistogramImplConfig(
            type,
            chunkCount,
            chunkResetPeriodMs,
            scale,
            bucketsResettable,
            snapshotTtl);
    }
}
