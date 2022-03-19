package com.ringcentral.platform.metrics.defaultImpl.histogram.scale.configs;

import com.ringcentral.platform.metrics.defaultImpl.histogram.configs.*;
import com.ringcentral.platform.metrics.scale.Scale;

import java.time.Duration;
import java.util.Optional;

import static com.ringcentral.platform.metrics.scale.SpecificScaleBuilder.points;
import static com.ringcentral.platform.metrics.utils.TimeUnitUtils.MS_PER_SEC;
import static java.lang.Long.MAX_VALUE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "ConstantConditions" })
public class ScaleHistogramImplConfig extends AbstractHistogramImplConfig {

    public static final ScaleHistogramImplType DEFAULT_TYPE = ScaleHistogramImplType.RESET_BY_CHUNKS;

    public static final int MAX_CHUNKS = 60;
    public static final long MIN_CHUNK_RESET_PERIOD_MS = MS_PER_SEC;
    public static final int DEFAULT_CHUNKS = 6;
    public static final long DEFAULT_CHUNK_RESET_PERIOD_MS = 20L * MS_PER_SEC;

    public static final Scale DEFAULT_SCALE = points(
        MILLISECONDS,
        5, 10, 25, 50, 75, 100, 250, 500, 750, 1000,
        2500, 5000, 7500, 10000, MAX_VALUE).build();

    public static final int DEFAULT_SCALE_SPLIT_FACTOR_FOR_PERCENTILES = 1;
    public static final int DEFAULT_MAX_LAZY_TREE_LEVEL = 4;

    public static final ScaleHistogramImplConfig DEFAULT = new ScaleHistogramImplConfig(
        DEFAULT_TYPE,
        DEFAULT_CHUNKS,
        DEFAULT_CHUNK_RESET_PERIOD_MS,
        DEFAULT_SCALE,
        DEFAULT_SCALE_SPLIT_FACTOR_FOR_PERCENTILES,
        DEFAULT_MAX_LAZY_TREE_LEVEL,
        DEFAULT_TOTALS_MEASUREMENT_TYPE,
        DEFAULT_BUCKETS_MEASUREMENT_TYPE,
        Optional.ofNullable(DEFAULT_SNAPSHOT_TTL));

    private final ScaleHistogramImplType type;
    private final int chunkCount;
    private final long chunkResetPeriodMs;
    private final Scale scale;
    private final int scaleSplitFactorForPercentiles;
    private final int maxLazyTreeLevel;

    public ScaleHistogramImplConfig(
        ScaleHistogramImplType type,
        int chunkCount,
        long chunkResetPeriodMs,
        Scale scale,
        int scaleSplitFactorForPercentiles,
        int maxLazyTreeLevel,
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
        this.scale = scale;
        this.scaleSplitFactorForPercentiles = scaleSplitFactorForPercentiles;
        this.maxLazyTreeLevel = maxLazyTreeLevel;
    }

    public ScaleHistogramImplType type() {
        return type;
    }

    public int chunkCount() {
        return chunkCount;
    }

    public long chunkResetPeriodMs() {
        return chunkResetPeriodMs;
    }

    public Scale scale() {
        return scale;
    }

    public int scaleSplitFactorForPercentiles() {
        return scaleSplitFactorForPercentiles;
    }

    public int maxLazyTreeLevel() {
        return maxLazyTreeLevel;
    }
}
