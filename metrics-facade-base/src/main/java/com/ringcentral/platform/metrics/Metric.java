package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.names.MetricNamed;

import static com.ringcentral.platform.metrics.utils.StringUtils.isNotBlank;

public interface Metric extends MetricNamed, Iterable<MetricInstance> {
    boolean isEnabled();

    default boolean hasDescription() {
        return isNotBlank(description());
    }

    String description();

    void addListener(MetricListener listener);

    default void metricAdded() {}
    default void metricRemoved() {}
}