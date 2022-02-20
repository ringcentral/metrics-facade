package com.ringcentral.platform.metrics.defaultImpl.histogram;

public interface HistogramImpl {
    void update(long value);
    HistogramSnapshot snapshot();
    default void metricInstanceAdded() {}
    default void metricInstanceRemoved() {}
}
