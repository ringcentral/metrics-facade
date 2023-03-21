package com.ringcentral.platform.metrics.var.stringVar;

import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.AbstractVarInstance;

import java.util.List;
import java.util.function.Supplier;

public class DefaultStringVarInstance extends AbstractVarInstance<String> implements StringVarInstance {

    public DefaultStringVarInstance(
        MetricName name,
        List<LabelValue> labelValues,
        boolean totalInstance,
        boolean labeledMetricTotalInstance,
        boolean nonDecreasing,
        Measurable valueMeasurable,
        Supplier<String> valueSupplier) {

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
