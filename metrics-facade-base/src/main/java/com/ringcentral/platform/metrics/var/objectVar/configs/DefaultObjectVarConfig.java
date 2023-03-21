package com.ringcentral.platform.metrics.var.objectVar.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.labels.*;
import com.ringcentral.platform.metrics.var.configs.AbstractVarConfig;

import java.util.List;

public class DefaultObjectVarConfig extends AbstractVarConfig implements ObjectVarConfig {

    public DefaultObjectVarConfig(
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
