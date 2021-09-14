package com.ringcentral.platform.metrics.measurables;

import com.ringcentral.platform.metrics.MetricInstance;

public interface MeasurableNameProvider {
    String nameFor(MetricInstance instance, Measurable measurable);
}
