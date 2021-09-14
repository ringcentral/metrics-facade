package com.ringcentral.platform.metrics.var.configs;

import java.util.List;
import com.ringcentral.platform.metrics.configs.MetricConfig;
import com.ringcentral.platform.metrics.dimensions.MetricDimension;

public interface VarConfig extends MetricConfig {
    default boolean hasDimensions() {
        return dimensions() != null && !dimensions().isEmpty();
    }

    List<MetricDimension> dimensions();
}