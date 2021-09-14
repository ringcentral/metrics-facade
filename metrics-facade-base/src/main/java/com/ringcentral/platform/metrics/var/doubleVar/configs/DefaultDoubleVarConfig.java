package com.ringcentral.platform.metrics.var.doubleVar.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.var.configs.AbstractVarConfig;

import java.util.List;

public class DefaultDoubleVarConfig extends AbstractVarConfig implements DoubleVarConfig {

    public DefaultDoubleVarConfig(
        boolean enabled,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricContext context) {

        super(
            enabled,
            prefixDimensionValues,
            dimensions,
            context);
    }
}
