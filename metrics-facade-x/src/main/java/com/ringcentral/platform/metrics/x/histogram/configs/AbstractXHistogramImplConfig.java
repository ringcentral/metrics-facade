package com.ringcentral.platform.metrics.x.histogram.configs;

import java.time.Duration;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class AbstractXHistogramImplConfig implements XHistogramImplConfig {

    private final TotalsMeasurementType totalsMeasurementType;
    private final BucketsMeasurementType bucketsMeasurementType;
    private final Optional<Duration> snapshotTtl;

    @SuppressWarnings("ConstantConditions")
    protected AbstractXHistogramImplConfig() {
        this(
            DEFAULT_TOTALS_MEASUREMENT_TYPE,
            DEFAULT_BUCKETS_MEASUREMENT_TYPE,
            Optional.ofNullable(DEFAULT_SNAPSHOT_TTL));
    }

    protected AbstractXHistogramImplConfig(
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
