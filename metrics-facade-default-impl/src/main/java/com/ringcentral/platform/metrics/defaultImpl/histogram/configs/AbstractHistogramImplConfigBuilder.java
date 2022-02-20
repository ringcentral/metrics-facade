package com.ringcentral.platform.metrics.defaultImpl.histogram.configs;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static com.ringcentral.platform.metrics.defaultImpl.histogram.configs.HistogramImplConfig.*;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;

@SuppressWarnings({ "ConstantConditions", "OptionalUsedAsFieldOrParameterType", "unchecked" })
public abstract class AbstractHistogramImplConfigBuilder<
    C extends HistogramImplConfig,
    CB extends AbstractHistogramImplConfigBuilder<C, CB>> implements HistogramImplConfigBuilder<C> {

    protected TotalsMeasurementType totalsMeasurementType = DEFAULT_TOTALS_MEASUREMENT_TYPE;
    protected BucketsMeasurementType bucketsMeasurementType = DEFAULT_BUCKETS_MEASUREMENT_TYPE;
    protected Optional<Duration> snapshotTtl = Optional.ofNullable(DEFAULT_SNAPSHOT_TTL);

    public CB consistentTotals() {
        this.totalsMeasurementType = TotalsMeasurementType.CONSISTENT;
        return builder();
    }

    public CB eventuallyConsistentTotals() {
        this.totalsMeasurementType = TotalsMeasurementType.EVENTUALLY_CONSISTENT;
        return builder();
    }

    public CB neverResetBuckets() {
        this.bucketsMeasurementType = BucketsMeasurementType.NEVER_RESET;
        return builder();
    }

    public CB resettableBuckets() {
        this.bucketsMeasurementType = BucketsMeasurementType.RESETTABLE;
        return builder();
    }

    public CB snapshotTtl(long ttl, ChronoUnit ttlUnit) {
        checkArgument(ttl > 0L, "ttl must be positive");
        this.snapshotTtl = Optional.of(Duration.of(ttl, ttlUnit));
        return builder();
    }

    protected CB builder() {
        return (CB)this;
    }
}
