package com.ringcentral.platform.metrics.configs;

import java.util.Set;
import com.ringcentral.platform.metrics.MetricContext;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

public interface MeterInstanceConfig {
    default boolean hasName() {
        return name() != null;
    }

    MetricName name();

    default boolean hasMeasurables() {
        return measurables() != null && !measurables().isEmpty();
    }

    Set<? extends Measurable> measurables();
    MetricContext context();
}
