package com.ringcentral.platform.metrics.var.doubleVar.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.labels.*;
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
        LabelValues prefixLabelValues,
        List<Label> labels,
        boolean nonDecreasing,
        MetricContext context) {

        return new DefaultDoubleVarConfig(
            enabled,
            description,
            prefixLabelValues,
            labels,
            nonDecreasing,
            context);
    }
}
