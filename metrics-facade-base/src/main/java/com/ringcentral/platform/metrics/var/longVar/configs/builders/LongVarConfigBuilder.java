package com.ringcentral.platform.metrics.var.longVar.configs.builders;

import java.util.List;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.var.configs.builders.AbstractVarConfigBuilder;
import com.ringcentral.platform.metrics.var.longVar.configs.*;

public class LongVarConfigBuilder extends AbstractVarConfigBuilder<LongVarConfig, LongVarConfigBuilder> {

    public static LongVarConfigBuilder longVar() {
        return longVarConfigBuilder();
    }

    public static LongVarConfigBuilder withLongVar() {
        return longVarConfigBuilder();
    }

    public static LongVarConfigBuilder longVarConfigBuilder() {
        return new LongVarConfigBuilder();
    }

    @Override
    protected LongVarConfig buildImpl(
        boolean enabled,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricContext context) {

        return new DefaultLongVarConfig(
            enabled,
            prefixDimensionValues,
            dimensions,
            context);
    }
}
