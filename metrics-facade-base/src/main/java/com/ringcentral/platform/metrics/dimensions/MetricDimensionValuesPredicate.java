package com.ringcentral.platform.metrics.dimensions;

public interface MetricDimensionValuesPredicate {
    boolean matches(MetricDimensionValues dimensionValues);
}
