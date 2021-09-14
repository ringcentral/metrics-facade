package com.ringcentral.platform.metrics.var.configs;

import java.util.List;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.AbstractMetricConfig;
import com.ringcentral.platform.metrics.dimensions.*;

import static java.util.Collections.*;

public abstract class AbstractVarConfig extends AbstractMetricConfig implements VarConfig {

    private final List<MetricDimension> dimensions;

    protected AbstractVarConfig(
        boolean enabled,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricContext context) {

        super(enabled, prefixDimensionValues, context);
        this.dimensions = dimensions != null ? dimensions : emptyList();
    }

    @Override
    public List<MetricDimension> dimensions() {
        return dimensions;
    }
}
