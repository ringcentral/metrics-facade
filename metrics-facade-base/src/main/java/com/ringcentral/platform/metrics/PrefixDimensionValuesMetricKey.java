package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;
import com.ringcentral.platform.metrics.names.MetricName;

import static com.ringcentral.platform.metrics.utils.ObjectUtils.*;
import static com.ringcentral.platform.metrics.utils.Preconditions.*;
import static java.util.Objects.*;

public class PrefixDimensionValuesMetricKey implements MetricKey {

    private final MetricName name;
    private final MetricDimensionValues dimensionValues;
    private final int hashCode;

    public static PrefixDimensionValuesMetricKey withKey(MetricName name, MetricDimensionValues dimensionValues) {
        return prefixDimensionValuesMetricKey(name, dimensionValues);
    }

    public static PrefixDimensionValuesMetricKey metricKey(MetricName name, MetricDimensionValues dimensionValues) {
        return prefixDimensionValuesMetricKey(name, dimensionValues);
    }

    public static PrefixDimensionValuesMetricKey prefixDimensionValuesMetricKey(MetricName name, MetricDimensionValues dimensionValues) {
        return new PrefixDimensionValuesMetricKey(name, dimensionValues);
    }

    public PrefixDimensionValuesMetricKey(MetricName name, MetricDimensionValues dimensionValues) {
        this.name = requireNonNull(name);

        checkArgument(
            dimensionValues != null && !dimensionValues.isEmpty(),
            "dimensionValues is null or empty");

        this.dimensionValues = dimensionValues;
        this.hashCode = hashCodeFor(name, dimensionValues);
    }

    @Override
    public MetricName name() {
        return name;
    }

    public MetricDimensionValues dimensionValues() {
        return dimensionValues;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        PrefixDimensionValuesMetricKey that = (PrefixDimensionValuesMetricKey)other;

        if (hashCode != that.hashCode) {
            return false;
        }

        return name.equals(that.name) && dimensionValues.equals(that.dimensionValues);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
