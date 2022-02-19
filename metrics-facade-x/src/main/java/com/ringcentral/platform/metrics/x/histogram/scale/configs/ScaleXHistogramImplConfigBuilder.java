package com.ringcentral.platform.metrics.x.histogram.scale.configs;

import com.ringcentral.platform.metrics.scale.*;
import com.ringcentral.platform.metrics.x.histogram.configs.AbstractXHistogramImplConfigBuilder;

import java.time.Duration;

import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static com.ringcentral.platform.metrics.x.histogram.scale.configs.ScaleXHistogramImplConfig.*;
import static java.util.Objects.requireNonNull;

public class ScaleXHistogramImplConfigBuilder extends AbstractXHistogramImplConfigBuilder<ScaleXHistogramImplConfig, ScaleXHistogramImplConfigBuilder> {

    public static final int NO_LAZY_TREE_LEVELS = -1;

    private ScaleXHistogramImplType type = DEFAULT.type();
    private int chunkCount = DEFAULT.chunkCount();
    private long chunkResetPeriodMs = DEFAULT.chunkResetPeriodMs();
    private Scale scale = DEFAULT.scale();
    private int scaleSplitFactorForPercentiles = DEFAULT.scaleSplitFactorForPercentiles();
    private int maxLazyTreeLevel = DEFAULT.maxLazyTreeLevel();

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
        this.type = ScaleXHistogramImplType.NEVER_RESET;
        return this;
    }

    public ScaleXHistogramImplConfigBuilder resetOnSnapshot() {
        this.type = ScaleXHistogramImplType.RESET_ON_SNAPSHOT;
        return this;
    }

    public ScaleXHistogramImplConfigBuilder resetPeriodically(Duration period) {
        this.type = ScaleXHistogramImplType.RESET_BY_CHUNKS;
        return resetByChunks(2, 2L * period.toMillis());
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

    public ScaleXHistogramImplConfigBuilder resetByChunks() {
        return resetByChunks(DEFAULT_CHUNKS, DEFAULT_CHUNKS * DEFAULT_CHUNK_RESET_PERIOD_MS);
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

    public ScaleXHistogramImplConfigBuilder scaleSplitFactorForPercentiles(int scaleSplitFactorForPercentiles) {
        checkArgument(scaleSplitFactorForPercentiles > 0, "scaleSplitFactorForPercentiles must be > 0");
        this.scaleSplitFactorForPercentiles = scaleSplitFactorForPercentiles;
        return this;
    }

    public ScaleXHistogramImplConfigBuilder noLazyTreeLevels() {
        return maxLazyTreeLevel(NO_LAZY_TREE_LEVELS);
    }

    public ScaleXHistogramImplConfigBuilder maxLazyTreeLevel(int maxLazyTreeLevel) {
        this.maxLazyTreeLevel = maxLazyTreeLevel;
        return this;
    }

    public ScaleXHistogramImplConfig build() {
        return new ScaleXHistogramImplConfig(
            type,
            chunkCount,
            chunkResetPeriodMs,
            scale,
            scaleSplitFactorForPercentiles,
            maxLazyTreeLevel,
            totalsMeasurementType,
            bucketsMeasurementType,
            snapshotTtl);
    }
}
