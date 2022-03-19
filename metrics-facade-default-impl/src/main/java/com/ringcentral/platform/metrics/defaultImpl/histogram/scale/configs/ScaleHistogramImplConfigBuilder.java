package com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs;

import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.AbstractHistogramImplConfigBuilder;
import com.ringcentral.platform.metrics.scale.*;

import java.time.Duration;

import static com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs.ScaleHistogramImplConfig.*;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class ScaleHistogramImplConfigBuilder extends AbstractHistogramImplConfigBuilder<ScaleHistogramImplConfig, ScaleHistogramImplConfigBuilder> {

    public static final int NO_LAZY_TREE_LEVELS = -1;

    private ScaleHistogramImplType type = DEFAULT.type();
    private int chunkCount = DEFAULT.chunkCount();
    private long chunkResetPeriodMs = DEFAULT.chunkResetPeriodMs();
    private Scale scale = DEFAULT.scale();
    private int scaleSplitFactorForPercentiles = DEFAULT.scaleSplitFactorForPercentiles();
    private int maxLazyTreeLevel = DEFAULT.maxLazyTreeLevel();

    public static ScaleHistogramImplConfigBuilder scale() {
        return scaleHistogramImplConfigBuilder();
    }

    public static ScaleHistogramImplConfigBuilder scaleImpl() {
        return scaleHistogramImplConfigBuilder();
    }

    public static ScaleHistogramImplConfigBuilder scaleHistogramImplConfigBuilder() {
        return new ScaleHistogramImplConfigBuilder();
    }

    public ScaleHistogramImplConfigBuilder neverReset() {
        this.type = ScaleHistogramImplType.NEVER_RESET;
        return this;
    }

    public ScaleHistogramImplConfigBuilder resetOnSnapshot() {
        this.type = ScaleHistogramImplType.RESET_ON_SNAPSHOT;
        return this;
    }

    public ScaleHistogramImplConfigBuilder resetPeriodically(Duration period) {
        return resetByChunks(1, period.toMillis());
    }

    private ScaleHistogramImplConfigBuilder resetByChunks(int chunkCount, long chunkResetPeriodMs) {
        checkArgument(chunkCount >= 0, "chunkCount must be >= 0");
        checkArgument(chunkCount <= MAX_CHUNKS, "chunkCount must be <= " + MAX_CHUNKS);

        checkArgument(
            chunkResetPeriodMs >= MIN_CHUNK_RESET_PERIOD_MS,
            "chunkResetPeriodMs must be >= " + MIN_CHUNK_RESET_PERIOD_MS + " ms");

        this.type = ScaleHistogramImplType.RESET_BY_CHUNKS;
        this.chunkCount = chunkCount;
        this.chunkResetPeriodMs = chunkResetPeriodMs;
        return this;
    }

    public ScaleHistogramImplConfigBuilder resetByChunks() {
        return resetByChunks(DEFAULT_CHUNKS, DEFAULT_CHUNK_RESET_PERIOD_MS);
    }

    public ScaleHistogramImplConfigBuilder resetByChunks(int chunkCount, Duration allChunksResetPeriod) {
        checkArgument(chunkCount >= 2, "chunkCount must be >= 2");
        return resetByChunks(chunkCount, allChunksResetPeriod.toMillis() / chunkCount);
    }

    public ScaleHistogramImplConfigBuilder with(ScaleBuilder<?> scaleBuilder) {
        return scale(scaleBuilder.build());
    }

    public ScaleHistogramImplConfigBuilder with(Scale scale) {
        this.scale = requireNonNull(scale);
        return this;
    }

    public ScaleHistogramImplConfigBuilder scale(ScaleBuilder<?> scaleBuilder) {
        return scale(scaleBuilder.build());
    }

    public ScaleHistogramImplConfigBuilder scale(Scale scale) {
        this.scale = requireNonNull(scale);
        return this;
    }

    public ScaleHistogramImplConfigBuilder scaleSplitFactorForPercentiles(int scaleSplitFactorForPercentiles) {
        checkArgument(scaleSplitFactorForPercentiles > 0, "scaleSplitFactorForPercentiles must be > 0");
        this.scaleSplitFactorForPercentiles = scaleSplitFactorForPercentiles;
        return this;
    }

    public ScaleHistogramImplConfigBuilder noLazyTreeLevels() {
        return maxLazyTreeLevel(NO_LAZY_TREE_LEVELS);
    }

    public ScaleHistogramImplConfigBuilder maxLazyTreeLevel(int maxLazyTreeLevel) {
        this.maxLazyTreeLevel = maxLazyTreeLevel;
        return this;
    }

    public ScaleHistogramImplConfig build() {
        return new ScaleHistogramImplConfig(
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
