package com.ringcentral.platform.metrics.dimensions;

import static com.ringcentral.platform.metrics.utils.ObjectUtils.*;
import static java.util.Objects.*;

public class MetricDimensionValue {

    private final MetricDimension dimension;
    private final String value;
    private final int hashCode;

    public MetricDimensionValue(MetricDimension dimension, String value) {
        this.dimension = requireNonNull(dimension);
        this.value = requireNonNull(value);
        this.hashCode = hashCodeFor(dimension, value);
    }

    public MetricDimension dimension() {
        return dimension;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        MetricDimensionValue that = (MetricDimensionValue)other;

        if (hashCode != that.hashCode) {
            return false;
        }

        return dimension.equals(that.dimension) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "MetricDimensionValue{" +
            "dimension=" + dimension +
            ", value='" + value + '\'' +
            '}';
    }
}
