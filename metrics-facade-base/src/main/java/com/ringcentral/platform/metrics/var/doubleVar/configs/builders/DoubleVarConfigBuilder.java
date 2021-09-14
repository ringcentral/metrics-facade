package com.ringcentral.platform.metrics.var.doubleVar.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.var.configs.builders.AbstractVarConfigBuilder;
import com.ringcentral.platform.metrics.var.doubleVar.configs.*;

import java.util.List;

public class DoubleVarConfigBuilder extends AbstractVarConfigBuilder<DoubleVarConfig, DoubleVarConfigBuilder> {

    public static DoubleVarConfigBuilder doubleVar() {
        return doubleVarConfigBuilder();
    }

    public static DoubleVarConfigBuilder withDoubleVar() {
        return doubleVarConfigBuilder();
    }

    public static DoubleVarConfigBuilder doubleVarConfigBuilder() {
        return new DoubleVarConfigBuilder();
    }

    @Override
    protected DoubleVarConfig buildImpl(
        boolean enabled,
        String description,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        boolean nonDecreasing,
        MetricContext context) {

        return new DefaultDoubleVarConfig(
            enabled,
            description,
            prefixDimensionValues,
            dimensions,
            nonDecreasing,
            context);
    }
}
