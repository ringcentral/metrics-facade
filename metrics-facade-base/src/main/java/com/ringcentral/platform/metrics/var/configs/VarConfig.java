package com.ringcentral.platform.metrics.var.configs;

import com.ringcentral.platform.metrics.configs.MetricConfig;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;

import java.util.List;

public interface VarConfig extends MetricConfig {
    default boolean hasDimensions() {
        return dimensions() != null && !dimensions().isEmpty();
    }

    List<MetricDimension> dimensions();

    boolean isNonDecreasing();
}