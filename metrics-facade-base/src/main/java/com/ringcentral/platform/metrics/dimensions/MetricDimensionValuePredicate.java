package com.ringcentral.platform.metrics.dimensions;

public interface MetricDimensionValuePredicate {
    MetricDimension dimension();
    boolean matches(String value);
}
