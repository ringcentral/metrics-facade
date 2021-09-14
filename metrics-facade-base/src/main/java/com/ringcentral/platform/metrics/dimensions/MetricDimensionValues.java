package com.ringcentral.platform.metrics.dimensions;

import java.util.List;

public class MetricDimensionValues {

    public static final MetricDimensionValues NO_DIMENSION_VALUES = dimensionValues();

    private final List<MetricDimensionValue> list;

    public static MetricDimensionValues noDimensionValues() {
        return NO_DIMENSION_VALUES;
    }

    public static MetricDimensionValues forDimensionValues(MetricDimensionValue... values) {
        return dimensionValues(values);
    }

    public static MetricDimensionValues dimensionValues(MetricDimensionValue... values) {
        return new MetricDimensionValues(values);
    }

    private MetricDimensionValues(MetricDimensionValue[] values) {
        this.list = List.of(values);
    }

    public List<MetricDimensionValue> list() {
        return list;
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        MetricDimensionValues that = (MetricDimensionValues)other;
        return list.equals(that.list);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public String toString() {
        return "MetricDimensionValues{" +
            "list=" + list +
            '}';
    }
}
