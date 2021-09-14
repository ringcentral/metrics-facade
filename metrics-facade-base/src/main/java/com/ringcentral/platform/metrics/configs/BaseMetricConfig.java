package com.ringcentral.platform.metrics.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;

public class BaseMetricConfig extends AbstractMetricConfig {

    public BaseMetricConfig(
        boolean enabled,
        MetricDimensionValues prefixDimensionValues,
        MetricContext context) {

        super(
            enabled,
            prefixDimensionValues,
            context);
    }
}
