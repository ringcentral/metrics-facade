package com.ringcentral.platform.metrics.var.configs;

import java.util.List;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;

public class BaseVarConfig extends AbstractVarConfig {

    public BaseVarConfig(
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
