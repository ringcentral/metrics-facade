package com.ringcentral.platform.metrics.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;

import static com.ringcentral.platform.metrics.UnmodifiableMetricContext.*;
import static com.ringcentral.platform.metrics.dimensions.MetricDimensionValues.*;

public abstract class AbstractMetricConfig implements MetricConfig {

    private final boolean enabled;
    private final MetricDimensionValues prefixDimensionValues;
    private final MetricContext context;

    protected AbstractMetricConfig(
        boolean enabled,
        MetricDimensionValues prefixDimensionValues,
        MetricContext context) {

        this.enabled = enabled;
        this.prefixDimensionValues = prefixDimensionValues != null ? prefixDimensionValues : noDimensionValues();
        this.context = context != null ? context : emptyUnmodifiableMetricContext();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public MetricDimensionValues prefixDimensionValues() {
        return prefixDimensionValues;
    }

    @Override
    public MetricContext context() {
        return context;
    }
}
