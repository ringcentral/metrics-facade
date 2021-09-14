package com.ringcentral.platform.metrics.var.doubleVar.configs.builders;

import java.util.List;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.var.configs.builders.*;
import com.ringcentral.platform.metrics.var.doubleVar.configs.*;

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
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricContext context) {

        return new DefaultDoubleVarConfig(
            enabled,
            prefixDimensionValues,
            dimensions,
            context);
    }
}
