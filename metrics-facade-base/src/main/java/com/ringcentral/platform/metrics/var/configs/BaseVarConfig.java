package com.ringcentral.platform.metrics.var.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.labels.*;

import java.util.List;

public class BaseVarConfig extends AbstractVarConfig {

    public BaseVarConfig(
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
