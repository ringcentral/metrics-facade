package com.ringcentral.platform.metrics.var.longVar.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.labels.*;
import com.ringcentral.platform.metrics.var.configs.builders.AbstractVarConfigBuilder;
import com.ringcentral.platform.metrics.var.longVar.configs.*;

import java.util.List;

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
        String description,
        LabelValues prefixLabelValues,
        List<Label> labels,
        boolean nonDecreasing,
        MetricContext context) {

        return new DefaultLongVarConfig(
            enabled,
            description,
            prefixLabelValues,
            labels,
            nonDecreasing,
            context);
    }
}
