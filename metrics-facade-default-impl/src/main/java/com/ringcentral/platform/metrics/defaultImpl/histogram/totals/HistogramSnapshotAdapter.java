package com.ringcentral.platform.metrics.defaultImpl.histogram.totals;

import com.ringcentral.platform.metrics.defaultImpl.histogram.HistogramSnapshot;
import com.ringcentral.platform.metrics.histogram.Histogram;

public abstract class HistogramSnapshotAdapter implements HistogramSnapshot {

    @Override
    public long count() {
        return NO_VALUE;
    }

    @Override
    public long totalSum() {
        return NO_VALUE;
    }

    @Override
    public long min() {
        return NO_VALUE;
    }

    @Override
    public long max() {
        return NO_VALUE;
    }

    @Override
    public double mean() {
        return NO_VALUE_DOUBLE;
    }

    @Override
    public double standardDeviation() {
        return NO_VALUE_DOUBLE;
    }

    @Override
    public double percentileValue(Histogram.Percentile percentile) {
        return NO_VALUE_DOUBLE;
    }

    @Override
    public long bucketSize(Histogram.Bucket bucket) {
        return NO_VALUE;
    }
}
