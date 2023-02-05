package com.ringcentral.platform.metrics.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.labels.LabelValues;

import static org.apache.commons.lang3.StringUtils.isBlank;

public interface MetricConfig {
    boolean isEnabled();

    default boolean hasDescription() {
        return !isBlank(description());
    }

    String description();

    default boolean hasPrefixLabelValues() {
        return prefixLabelValues() != null && !prefixLabelValues().isEmpty();
    }

    LabelValues prefixLabelValues();
    MetricContext context();
}
