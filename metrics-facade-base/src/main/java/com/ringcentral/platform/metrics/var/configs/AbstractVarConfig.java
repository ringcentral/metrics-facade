package com.ringcentral.platform.metrics.var.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.AbstractMetricConfig;
import com.ringcentral.platform.metrics.dimensions.*;

import java.util.List;

import static java.util.Collections.emptyList;

public abstract class AbstractVarConfig extends AbstractMetricConfig implements VarConfig {

    private final List<MetricDimension> dimensions;
    private final boolean nonDecreasing;

    protected AbstractVarConfig(
        boolean enabled,
        String description,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        boolean nonDecreasing,
        MetricContext context) {

        super(
            enabled,
            description,
            prefixDimensionValues,
            context);

        this.nonDecreasing = nonDecreasing;
        this.dimensions = dimensions != null ? dimensions : emptyList();
    }

    @Override
    public List<MetricDimension> dimensions() {
        return dimensions;
    }

    @Override
    public boolean isNonDecreasing() {
        return nonDecreasing;
    }
}
