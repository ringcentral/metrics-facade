package com.ringcentral.platform.metrics.x.histogram;

public interface XHistogramImplSnapshot {
    long size();
    long min();
    long max();
    double mean();
    double standardDeviation();
    double percentile(double quantile);
}
