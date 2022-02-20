package com.ringcentral.platform.metrics.defaultImpl.histogram.configs;

import java.time.Duration;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class AbstractHistogramImplConfig implements HistogramImplConfig {

    private final TotalsMeasurementType totalsMeasurementType;
    private final BucketsMeasurementType bucketsMeasurementType;
    private final Optional<Duration> snapshotTtl;

    @SuppressWarnings("ConstantConditions")
    protected AbstractHistogramImplConfig() {
        this(
            DEFAULT_TOTALS_MEASUREMENT_TYPE,
            DEFAULT_BUCKETS_MEASUREMENT_TYPE,
            Optional.ofNullable(DEFAULT_SNAPSHOT_TTL));
    }

    protected AbstractHistogramImplConfig(
        TotalsMeasurementType totalsMeasurementType,
        BucketsMeasurementType bucketsMeasurementType,
        Optional<Duration> snapshotTtl) {

        this.totalsMeasurementType = requireNonNull(totalsMeasurementType);
        this.bucketsMeasurementType = requireNonNull(bucketsMeasurementType);
        this.snapshotTtl = requireNonNull(snapshotTtl);
    }

    @Override
    public TotalsMeasurementType totalsMeasurementType() {
        return totalsMeasurementType;
    }

    @Override
    public BucketsMeasurementType bucketsMeasurementType() {
        return bucketsMeasurementType;
    }

    @Override
    public Optional<Duration> snapshotTtl() {
        return snapshotTtl;
    }
}
