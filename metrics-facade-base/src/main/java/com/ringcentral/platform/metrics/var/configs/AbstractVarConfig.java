package com.ringcentral.platform.metrics.var.configs;

import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.configs.AbstractMetricConfig;
import com.ringcentral.platform.metrics.labels.*;

import java.util.List;

import static java.util.Collections.emptyList;

public abstract class AbstractVarConfig extends AbstractMetricConfig implements VarConfig {

    private final List<Label> labels;
    private final boolean nonDecreasing;

    protected AbstractVarConfig(
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
            context);

        this.nonDecreasing = nonDecreasing;
        this.labels = labels != null ? labels : emptyList();
    }

    @Override
    public List<Label> labels() {
        return labels;
    }

    @Override
    public boolean isNonDecreasing() {
        return nonDecreasing;
    }
}
