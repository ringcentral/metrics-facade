package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.names.MetricNamed;

import static org.apache.commons.lang3.StringUtils.isBlank;

public interface Metric extends MetricNamed, Iterable<MetricInstance> {
    boolean isEnabled();

    default boolean hasDescription() {
        return !isBlank(description());
    }

    String description();

    void addListener(MetricListener listener);

    default void metricAdded() {}
    default void metricRemoved() {}
}