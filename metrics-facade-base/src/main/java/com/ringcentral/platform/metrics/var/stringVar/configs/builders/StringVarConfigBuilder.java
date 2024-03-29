package com.ringcentral.platform.metrics.var.stringVar.configs.builders;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.labels.*;
import com.ringcentral.platform.metrics.var.configs.builders.AbstractVarConfigBuilder;
import com.ringcentral.platform.metrics.var.stringVar.configs.*;

import java.util.List;

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
        String description,
        LabelValues prefixLabelValues,
        List<Label> labels,
        boolean nonDecreasing,
        MetricContext context) {

        return new DefaultStringVarConfig(
            enabled,
            description,
            prefixLabelValues,
            labels,
            nonDecreasing,
            context);
    }
}