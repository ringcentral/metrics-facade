package com.ringcentral.platform.metrics.x.histogram;

import com.ringcentral.platform.metrics.histogram.Histogram.*;

public interface XHistogramSnapshot {
    long NO_VALUE = 0L;

    long count();
    long totalSum();
    long min();
    long max();
    double mean();
    double standardDeviation();
    double percentileValue(Percentile percentile);
    long bucketSize(Bucket bucket);
}
