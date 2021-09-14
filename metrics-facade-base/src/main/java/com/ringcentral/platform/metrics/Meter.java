package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;

public interface Meter extends Metric {
    void removeInstancesFor(MetricDimensionValues dimensionValues);
}
