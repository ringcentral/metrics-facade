package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.histogram.Histogram;
import com.ringcentral.platform.metrics.labels.Label;
import com.ringcentral.platform.metrics.labels.LabelUtils;
import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.measurables.MeasurableValues;
import com.ringcentral.platform.metrics.names.MetricNamed;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.ringcentral.platform.metrics.utils.CollectionUtils.isNonEmpty;

public interface MetricInstance extends MetricNamed {
    default boolean hasLabelValues() {
        return isNonEmpty(labelValues());
    }

    List<LabelValue> labelValues();

    default boolean hasLabel(Label label) {
        return LabelUtils.hasLabel(labelValues(), label);
    }

    default String valueOf(Label label) {
        return LabelUtils.valueOf(labelValues(), label);
    }

    default Map<Label, LabelValue> labelToValue() {
        return LabelUtils.labelToValue(labelValues());
    }

    default List<LabelValue> labelValuesWithout(Label label, Label... labels) {
        return LabelUtils.labelValuesWithout(labelValues(), label, labels);
    }

    boolean isTotalInstance();
    boolean isLabeledMetricTotalInstance();
    boolean isLevelInstance();

    Set<Measurable> measurables();

    default boolean isWithPercentiles() {
        return measurables().stream().anyMatch(m -> m instanceof Histogram.Percentile);
    }

    default boolean isWithBuckets() {
        return measurables().stream().anyMatch(m -> m instanceof Histogram.Bucket);
    }

    MeasurableValues measurableValues();
    <V> V valueOf(Measurable measurable) throws NotMeasuredException;

    default void metricInstanceAdded() {}
    default void metricInstanceRemoved() {}
}
