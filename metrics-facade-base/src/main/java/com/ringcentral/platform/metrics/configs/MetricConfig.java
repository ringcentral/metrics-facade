package com.ringcentral.platform.metrics.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;

public interface MetricConfig {
    boolean isEnabled();

    default boolean hasPrefixDimensionValues() {
        return prefixDimensionValues() != null && !prefixDimensionValues().isEmpty();
    }

    MetricDimensionValues prefixDimensionValues();
    MetricContext context();
}
