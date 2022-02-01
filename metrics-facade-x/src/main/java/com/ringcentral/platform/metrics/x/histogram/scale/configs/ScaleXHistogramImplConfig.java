package com.ringcentral.platform.metrics.x.histogram.scale.configs;

import com.ringcentral.platform.metrics.x.histogram.configs.*;

import java.time.Duration;
import java.util.Optional;

import static com.ringcentral.platform.metrics.utils.TimeUnitUtils.MS_PER_SEC;
import static com.ringcentral.platform.metrics.x.histogram.scale.configs.ExpScaleBuilder.exp;

@SuppressWarnings({ "OptionalUsedAsFieldOrParameterType", "ConstantConditions" })
public class ScaleXHistogramImplConfig extends AbstractXHistogramImplConfig {

    public static final ScaleXHistogramImplType DEFAULT_TYPE = ScaleXHistogramImplType.RESET_BY_CHUNKS;

    public static final int MAX_CHUNKS = 60;
    public static final long MIN_CHUNK_RESET_PERIOD_MS = MS_PER_SEC;
    public static final int DEFAULT_CHUNKS = 6;
    public static final long DEFAULT_CHUNK_RESET_PERIOD_MS = 20L * MS_PER_SEC;
    public static final ExpScale DEFAULT_SCALE = exp().from(0).base(2).factor(2).build();

    public static final ScaleXHistogramImplConfig DEFAULT = new ScaleXHistogramImplConfig(
        DEFAULT_TYPE,
        DEFAULT_CHUNKS,
        DEFAULT_CHUNK_RESET_PERIOD_MS,
        DEFAULT_SCALE,
        DEFAULT_TOTALS_MEASUREMENT_TYPE,
        DEFAULT_BUCKETS_MEASUREMENT_TYPE,
        Optional.ofNullable(DEFAULT_SNAPSHOT_TTL));

    private final ScaleXHistogramImplType type;
    private final int chunkCount;
    private final long chunkResetPeriodMs;
    private final Scale scale;

    public ScaleXHistogramImplConfig(
        ScaleXHistogramImplType type,
        int chunkCount,
        long chunkResetPeriodMs,
        Scale scale,
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
    }

    public ScaleXHistogramImplType type() {
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
}
