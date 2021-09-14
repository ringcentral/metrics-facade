package com.ringcentral.platform.metrics;

public interface MetricListener {
    default void metricInstanceAdded(MetricInstance instance) {}
    default void metricInstanceRemoved(MetricInstance instance) {}
}
