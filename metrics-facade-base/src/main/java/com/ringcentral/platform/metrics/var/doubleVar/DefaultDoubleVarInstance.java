package com.ringcentral.platform.metrics.var.doubleVar;

import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.AbstractVarInstance;

import java.util.List;
import java.util.function.Supplier;

public class DefaultDoubleVarInstance extends AbstractVarInstance<Double> implements DoubleVarInstance {

    public DefaultDoubleVarInstance(
        MetricName name,
        List<LabelValue> labelValues,
        boolean totalInstance,
        boolean labeledMetricTotalInstance,
        boolean nonDecreasing,
        Measurable valueMeasurable,
        Supplier<Double> valueSupplier) {

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
