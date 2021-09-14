package com.ringcentral.platform.metrics.var.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.var.configs.BaseVarConfig;

import java.util.List;

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
        String description,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        boolean nonDecreasing,
        MetricContext context) {

        return new BaseVarConfig(
            enabled,
            description,
            prefixDimensionValues,
            dimensions,
            nonDecreasing,
            context);
    }
}
