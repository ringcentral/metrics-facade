package com.ringcentral.platform.metrics.var;

import com.ringcentral.platform.metrics.NotMeasuredException;
import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.measurables.*;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.*;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;

public abstract class AbstractVarInstance<V> implements VarInstance<V> {

    private final MetricName name;
    private final List<LabelValue> labelValues;
    private final boolean totalInstance;
    private final boolean labeledMetricTotalInstance;
    private final boolean nonDecreasing;
    private final Set<Measurable> measurables;
    private final MeasurableValues measurableValues;

    public AbstractVarInstance(
        MetricName name,
        List<LabelValue> labelValues,
        boolean totalInstance,
        boolean labeledMetricTotalInstance,
        boolean nonDecreasing,
        Measurable valueMeasurable,
        Supplier<V> valueSupplier) {

        this.name = name;
        this.labelValues = labelValues != null ? labelValues : emptyList();
        this.totalInstance = totalInstance;
        this.labeledMetricTotalInstance = labeledMetricTotalInstance;
        this.nonDecreasing = nonDecreasing;
        this.measurables = Set.of(valueMeasurable);

        this.measurableValues = new MeasurableValues() {

            @Override
            @SuppressWarnings("unchecked")
            public <T> T valueOf(Measurable measurable) throws NotMeasuredException {
                if (measurable != valueMeasurable) {
                    throw NotMeasuredException.forMeasurable(measurable);
                }

                return (T)valueSupplier.get();
            }
        };
    }

    @Override
    public MetricName name() {
        return name;
    }

    @Override
    public List<LabelValue> labelValues() {
        return labelValues;
    }

    @Override
    public boolean isTotalInstance() {
        return totalInstance;
    }

    @Override
    public boolean isLabeledMetricTotalInstance() {
        return labeledMetricTotalInstance;
    }

    @Override
    public boolean isLevelInstance() {
        return false;
    }

    @Override
    public boolean isNonDecreasing() {
        return nonDecreasing;
    }

    @Override
    public Set<Measurable> measurables() {
        return measurables;
    }

    @Override
    public MeasurableValues measurableValues() {
        return measurableValues;
    }

    @Override
    public <T> T valueOf(Measurable measurable) throws NotMeasuredException {
        return measurableValues.valueOf(measurable);
    }
}
