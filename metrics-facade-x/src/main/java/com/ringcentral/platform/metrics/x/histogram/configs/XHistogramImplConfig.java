package com.ringcentral.platform.metrics.x.histogram.configs;

import com.ringcentral.platform.metrics.*;

import java.time.Duration;
import java.util.Optional;

@MetricContextTypeKey
public interface XHistogramImplConfig extends MetricContextTypeKeySubtype {
    TotalsMeasurementType DEFAULT_TOTALS_MEASUREMENT_TYPE = TotalsMeasurementType.CONSISTENT;
    BucketsMeasurementType DEFAULT_BUCKETS_MEASUREMENT_TYPE = BucketsMeasurementType.NEVER_RESET;
    Duration DEFAULT_SNAPSHOT_TTL = null;

    TotalsMeasurementType totalsMeasurementType();
    BucketsMeasurementType bucketsMeasurementType();

    default boolean hasSnapshotTtl() {
        return snapshotTtl().isPresent();
    }

    Optional<Duration> snapshotTtl();
}
