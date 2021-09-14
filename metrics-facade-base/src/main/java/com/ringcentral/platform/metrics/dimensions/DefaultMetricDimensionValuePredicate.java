package com.ringcentral.platform.metrics.dimensions;

import java.util.function.Predicate;

import static java.util.Objects.*;

public class DefaultMetricDimensionValuePredicate implements MetricDimensionValuePredicate {

    private final MetricDimension dimension;
    private final Predicate<String> predicate;

    public DefaultMetricDimensionValuePredicate(MetricDimension dimension, Predicate<String> predicate) {
        this.dimension = requireNonNull(dimension);
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public MetricDimension dimension() {
        return dimension;
    }

    @Override
    public boolean matches(String value) {
        return predicate.test(value);
    }
}
