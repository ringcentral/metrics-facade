package com.ringcentral.platform.metrics.dimensions;

import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public class DefaultMetricDimensionValuesPredicate implements MetricDimensionValuesPredicate {

    private final Predicate<MetricDimensionValues> predicate;

    public static DefaultMetricDimensionValuesPredicate dimensionValuesMatching(Predicate<MetricDimensionValues> predicate) {
        return new DefaultMetricDimensionValuesPredicate(predicate);
    }

    public DefaultMetricDimensionValuesPredicate(Predicate<MetricDimensionValues> predicate) {
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public boolean matches(MetricDimensionValues dimensionValues) {
        return predicate.test(dimensionValues);
    }
}
