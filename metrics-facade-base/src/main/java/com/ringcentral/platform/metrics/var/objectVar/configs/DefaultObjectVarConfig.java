package com.ringcentral.platform.metrics.var.objectVar.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.var.configs.AbstractVarConfig;

import java.util.List;

public class DefaultObjectVarConfig extends AbstractVarConfig implements ObjectVarConfig {

    public DefaultObjectVarConfig(
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
