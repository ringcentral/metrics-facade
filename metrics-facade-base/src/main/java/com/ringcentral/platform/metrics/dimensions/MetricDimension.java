package com.ringcentral.platform.metrics.dimensions;

import java.util.function.Predicate;
import static java.util.Objects.*;

public class MetricDimension {

    private final String name;
    private final int hashCode;

    public MetricDimension(String name) {
        this.name = requireNonNull(name);
        this.hashCode = name.hashCode();
    }

    public String name() {
        return name;
    }

    public MetricDimensionValue value(String v) {
        return new MetricDimensionValue(this, v);
    }

    public MetricDimensionValueMask mask(String m) {
        return MetricDimensionValueMask.of(this, m);
    }

    public MetricDimensionValuePredicate predicate(Predicate<String> p) {
        return new DefaultMetricDimensionValuePredicate(this, p);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        MetricDimension that = (MetricDimension)other;

        if (hashCode != that.hashCode) {
            return false;
        }

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "MetricDimension{" +
            "name='" + name + '\'' +
            '}';
    }
}
