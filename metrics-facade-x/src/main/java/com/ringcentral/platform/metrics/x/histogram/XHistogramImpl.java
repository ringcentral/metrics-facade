package com.ringcentral.platform.metrics.x.histogram;

public interface XHistogramImpl {
    void update(long value);
    long count();
    long totalSum();
    XHistogramImplSnapshot snapshot();
}
