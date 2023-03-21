package com.ringcentral.platform.metrics.var.configs;

import com.ringcentral.platform.metrics.configs.MetricConfig;
import com.ringcentral.platform.metrics.labels.Label;

import java.util.List;

public interface VarConfig extends MetricConfig {
    default boolean hasLabels() {
        return labels() != null && !labels().isEmpty();
    }

    List<Label> labels();

    boolean isNonDecreasing();
}