package com.ringcentral.platform.metrics.var.longVar.configs;

import java.util.List;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.var.configs.AbstractVarConfig;

public class DefaultLongVarConfig extends AbstractVarConfig implements LongVarConfig {

    public DefaultLongVarConfig(
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
