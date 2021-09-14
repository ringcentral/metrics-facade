package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.names.MetricNamed;

public interface Metric extends MetricNamed, Iterable<MetricInstance> {
    boolean isEnabled();

    void addListener(MetricListener listener);

    default void metricAdded() {}
    default void metricRemoved() {}
}