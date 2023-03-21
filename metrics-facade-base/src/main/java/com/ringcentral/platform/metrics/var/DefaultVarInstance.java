package com.ringcentral.platform.metrics.var;

import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.List;
import java.util.function.Supplier;

public class DefaultVarInstance<V> extends AbstractVarInstance<V> {

    public DefaultVarInstance(
        MetricName name,
        List<LabelValue> labelValues,
        boolean totalInstance,
        boolean labeledMetricTotalInstance,
        boolean nonDecreasing,
        Measurable valueMeasurable,
        Supplier<V> valueSupplier) {

        super(
            name,
            labelValues,
            totalInstance,
            labeledMetricTotalInstance,
            nonDecreasing,
            valueMeasurable,
            valueSupplier);
    }
}
