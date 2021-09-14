package com.ringcentral.platform.metrics.var.stringVar;

import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.AbstractVarInstance;

import java.util.List;
import java.util.function.Supplier;

public class DefaultStringVarInstance extends AbstractVarInstance<String> implements StringVarInstance {

    public DefaultStringVarInstance(
        MetricName name,
        List<MetricDimensionValue> dimensionValues,
        boolean totalInstance,
        boolean dimensionalTotalInstance,
        boolean nonDecreasing,
        Measurable valueMeasurable,
        Supplier<String> valueSupplier) {

        super(
            name,
            dimensionValues,
            totalInstance,
            dimensionalTotalInstance,
            nonDecreasing,
            valueMeasurable,
            valueSupplier);
    }
}
