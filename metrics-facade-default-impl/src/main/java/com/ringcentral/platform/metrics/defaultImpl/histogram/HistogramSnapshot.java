package com.ringcentral.platform.metrics.defaultImpl.histogram;

import com.ringcentral.platform.metrics.histogram.Histogram.*;

public interface HistogramSnapshot {
    long NO_VALUE = 0L;
    double NO_VALUE_DOUBLE = 0.0;

    long count();
    long totalSum();
    long min();
    long max();
    double mean();
    double standardDeviation();
    double percentileValue(Percentile percentile);
    long bucketSize(Bucket bucket);
}
