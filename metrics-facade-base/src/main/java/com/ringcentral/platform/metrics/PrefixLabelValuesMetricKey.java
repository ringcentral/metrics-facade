package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.labels.LabelValues;
import com.ringcentral.platform.metrics.names.MetricName;

import static com.ringcentral.platform.metrics.utils.ObjectUtils.hashCodeFor;
import static com.ringcentral.platform.metrics.utils.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class PrefixLabelValuesMetricKey implements MetricKey {

    private final MetricName name;
    private final LabelValues labelValues;
    private final int hashCode;

    public static PrefixLabelValuesMetricKey withKey(MetricName name, LabelValues labelValues) {
        return prefixLabelValuesMetricKey(name, labelValues);
    }

    public static PrefixLabelValuesMetricKey metricKey(MetricName name, LabelValues labelValues) {
        return prefixLabelValuesMetricKey(name, labelValues);
    }

    public static PrefixLabelValuesMetricKey prefixLabelValuesMetricKey(MetricName name, LabelValues labelValues) {
        return new PrefixLabelValuesMetricKey(name, labelValues);
    }

    public PrefixLabelValuesMetricKey(MetricName name, LabelValues labelValues) {
        this.name = requireNonNull(name);

        checkArgument(
            labelValues != null && !labelValues.isEmpty(),
            "labelValues is null or empty");

        this.labelValues = labelValues;
        this.hashCode = hashCodeFor(name, labelValues);
    }

    @Override
    public MetricName name() {
        return name;
    }

    public LabelValues labelValues() {
        return labelValues;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        PrefixLabelValuesMetricKey that = (PrefixLabelValuesMetricKey)other;

        if (hashCode != that.hashCode) {
            return false;
        }

        return name.equals(that.name) && labelValues.equals(that.labelValues);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
