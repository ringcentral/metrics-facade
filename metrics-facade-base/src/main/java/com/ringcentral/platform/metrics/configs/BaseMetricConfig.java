package com.ringcentral.platform.metrics.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;

public class BaseMetricConfig extends AbstractMetricConfig {

    public BaseMetricConfig(
        boolean enabled,
        String description,
        MetricDimensionValues prefixDimensionValues,
        MetricContext context) {

        super(
            enabled,
            description,
            prefixDimensionValues,
            context);
    }
}
