package com.ringcentral.platform.metrics.var.stringVar.configs;

import java.util.List;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.var.configs.AbstractVarConfig;

public class DefaultStringVarConfig extends AbstractVarConfig implements StringVarConfig {

    public DefaultStringVarConfig(
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
