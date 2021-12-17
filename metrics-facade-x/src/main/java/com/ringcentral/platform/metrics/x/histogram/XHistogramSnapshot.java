package com.ringcentral.platform.metrics.x.histogram;

public interface XHistogramSnapshot {
    long min();
    long max();
    double mean();
    double standardDeviation();
    double percentile(double quantile);
}
