package com.ringcentral.platform.metrics.var.objectVar;

import com.ringcentral.platform.metrics.labels.LabelValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.*;

import java.util.List;
import java.util.function.Supplier;

public class DefaultObjectVarInstance extends AbstractVarInstance<Object> implements ObjectVarInstance {

    public DefaultObjectVarInstance(
        MetricName name,
        List<LabelValue> labelValues,
        boolean totalInstance,
        boolean labeledMetricTotalInstance,
        boolean nonDecreasing,
        Measurable valueMeasurable,
        Supplier<Object> valueSupplier) {

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
