package com.ringcentral.platform.metrics.var.doubleVar.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.labels.*;
import com.ringcentral.platform.metrics.var.configs.AbstractVarConfig;

import java.util.List;

public class DefaultDoubleVarConfig extends AbstractVarConfig implements DoubleVarConfig {

    public DefaultDoubleVarConfig(
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
