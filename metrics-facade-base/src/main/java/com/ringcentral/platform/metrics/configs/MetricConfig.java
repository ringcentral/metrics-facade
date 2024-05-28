package com.ringcentral.platform.metrics.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.labels.LabelValues;

import static com.ringcentral.platform.metrics.utils.StringUtils.isNotBlank;

public interface MetricConfig {
    boolean isEnabled();

    default boolean hasDescription() {
        return isNotBlank(description());
    }

    String description();

    default boolean hasPrefixLabelValues() {
        return prefixLabelValues() != null && !prefixLabelValues().isEmpty();
    }

    LabelValues prefixLabelValues();
    MetricContext context();
}
