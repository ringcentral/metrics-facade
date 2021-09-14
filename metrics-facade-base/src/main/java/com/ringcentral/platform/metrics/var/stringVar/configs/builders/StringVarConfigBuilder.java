package com.ringcentral.platform.metrics.var.stringVar.configs.builders;

import java.util.List;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.dimensions.*;
import com.ringcentral.platform.metrics.var.configs.builders.AbstractVarConfigBuilder;
import com.ringcentral.platform.metrics.var.stringVar.configs.*;

public class StringVarConfigBuilder extends AbstractVarConfigBuilder<StringVarConfig, StringVarConfigBuilder> {

    public static StringVarConfigBuilder stringVar() {
        return stringVarConfigBuilder();
    }

    public static StringVarConfigBuilder withStringVar() {
        return stringVarConfigBuilder();
    }

    public static StringVarConfigBuilder stringVarConfigBuilder() {
        return new StringVarConfigBuilder();
    }

    @Override
    protected StringVarConfig buildImpl(
        boolean enabled,
        MetricDimensionValues prefixDimensionValues,
        List<MetricDimension> dimensions,
        MetricContext context) {

        return new DefaultStringVarConfig(
            enabled,
            prefixDimensionValues,
            dimensions,
            context);
    }
}