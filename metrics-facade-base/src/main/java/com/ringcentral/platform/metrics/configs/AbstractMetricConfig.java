package com.ringcentral.platform.metrics.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.labels.LabelValues;

import static com.ringcentral.platform.metrics.UnmodifiableMetricContext.*;
import static com.ringcentral.platform.metrics.labels.LabelValues.*;

public abstract class AbstractMetricConfig implements MetricConfig {

    private final boolean enabled;
    private final String description;
    private final LabelValues prefixLabelValues;
    private final MetricContext context;

    protected AbstractMetricConfig(
        boolean enabled,
        String description,
        LabelValues prefixLabelValues,
        MetricContext context) {

        this.enabled = enabled;
        this.description = description;
        this.prefixLabelValues = prefixLabelValues != null ? prefixLabelValues : noLabelValues();
        this.context = context != null ? context : emptyUnmodifiableMetricContext();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public LabelValues prefixLabelValues() {
        return prefixLabelValues;
    }

    @Override
    public MetricContext context() {
        return context;
    }
}
