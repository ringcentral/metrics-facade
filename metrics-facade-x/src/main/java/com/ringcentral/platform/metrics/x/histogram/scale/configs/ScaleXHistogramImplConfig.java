package com.ringcentral.platform.metrics.x.histogram.scale.configs;

import com.ringcentral.platform.metrics.x.histogram.configs.XHistogramImplConfig;

import java.time.Duration;
import java.util.Optional;

import static com.ringcentral.platform.metrics.utils.TimeUnitUtils.MS_PER_SEC;
import static com.ringcentral.platform.metrics.x.histogram.scale.configs.CompositeScaleBuilder.compositeScaleBuilder;
import static com.ringcentral.platform.metrics.x.histogram.scale.configs.ExpScaleBuilder.exp;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ScaleXHistogramImplConfig implements XHistogramImplConfig {

    public static final ScaleXHistogramType DEFAULT_TYPE = ScaleXHistogramType.RESET_BY_CHUNKS;

    public static final int MAX_CHUNKS = 60;
    public static final long MIN_CHUNK_RESET_PERIOD_MS = MS_PER_SEC;
    public static final int DEFAULT_CHUNKS = 6;
    public static final long DEFAULT_CHUNK_RESET_PERIOD_MS = 20L * MS_PER_SEC;

    public static final ScaleXHistogramImplConfig DEFAULT = new ScaleXHistogramImplConfig(
        DEFAULT_TYPE,
        DEFAULT_CHUNKS,
        DEFAULT_CHUNK_RESET_PERIOD_MS,
        compositeScaleBuilder(exp().from(0).base(2).factor(2)).build(),
        DEFAULT_BUCKETS_RESETTABLE,
        Optional.empty());

    private final ScaleXHistogramType type;
    private final int chunkCount;
    private final long chunkResetPeriodMs;
    private final Scale scale;
    private final boolean bucketsResettable;
    private final Optional<Duration> snapshotTtl;

    public ScaleXHistogramImplConfig(
        ScaleXHistogramType type,
        int chunkCount,
        long chunkResetPeriodMs,
        Scale scale,
        boolean bucketsResettable,
        Optional<Duration> snapshotTtl) {

        this.type = type;
        this.chunkCount = chunkCount;
        this.chunkResetPeriodMs = chunkResetPeriodMs;
        this.scale = scale;
        this.bucketsResettable = bucketsResettable;
        this.snapshotTtl = requireNonNull(snapshotTtl);
    }

    public ScaleXHistogramType type() {
        return type;
    }

    public int chunkCount() {
        return chunkCount;
    }

    public long chunkResetPeriodMs() {
        return chunkResetPeriodMs;
    }

    public boolean hasSnapshotTtl() {
        return snapshotTtl.isPresent();
    }

    public Scale scale() {
        return scale;
    }

    @Override
    public boolean areBucketsResettable() {
        return bucketsResettable;
    }

    public Optional<Duration> snapshotTtl() {
        return snapshotTtl;
    }
}
