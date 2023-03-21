package com.ringcentral.platform.metrics.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.labels.LabelValues;

public class BaseMetricConfig extends AbstractMetricConfig {

    public BaseMetricConfig(
        boolean enabled,
        String description,
        LabelValues prefixLabelValues,
        MetricContext context) {

        super(
            enabled,
            description,
            prefixLabelValues,
            context);
    }
}
