package com.ringcentral.platform.metrics.var.stringVar.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.labels.*;
import com.ringcentral.platform.metrics.var.configs.AbstractVarConfig;

import java.util.List;

public class DefaultStringVarConfig extends AbstractVarConfig implements StringVarConfig {

    public DefaultStringVarConfig(
        boolean enabled,
        String description,
        LabelValues prefixLabelValues,
        List<Label> labels,
        boolean nonDecreasing,
        MetricContext context) {

        super(
            enabled,
            description,
            prefixLabelValues,
            labels,
            nonDecreasing,
            context);
    }
}
