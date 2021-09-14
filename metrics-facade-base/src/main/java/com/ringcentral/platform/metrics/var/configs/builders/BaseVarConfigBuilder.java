package com.ringcentral.platform.metrics.var.configs.builders;

import java.util.List;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.var.configs.BaseVarConfig;

public class BaseVarConfigBuilder extends AbstractVarConfigBuilder<BaseVarConfig, BaseVarConfigBuilder> {

    public static BaseVarConfigBuilder variable() {
        return varConfigBuilder();
    }

    public static BaseVarConfigBuilder withVar() {
        return varConfigBuilder();
    }

    public static BaseVarConfigBuilder varConfigBuilder() {
        return new BaseVarConfigBuilder();
    }

    @Override
    public BaseVarConfig buildImpl(
        boolean enabled,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricContext context) {

        return new BaseVarConfig(
            enabled,
            prefixDimensionValues,
            dimensions,
            context);
    }
}
