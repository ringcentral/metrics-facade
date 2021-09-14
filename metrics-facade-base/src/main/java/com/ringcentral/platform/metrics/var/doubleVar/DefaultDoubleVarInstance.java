package com.ringcentral.platform.metrics.var.doubleVar;

import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.AbstractVarInstance;

import java.util.List;
import java.util.function.Supplier;

public class DefaultDoubleVarInstance extends AbstractVarInstance<Double> implements DoubleVarInstance {

    public DefaultDoubleVarInstance(
        MetricName name,
        List<MetricDimensionValue> dimensionValues,
        boolean totalInstance,
        boolean dimensionalTotalInstance,
        boolean nonDecreasing,
        Measurable valueMeasurable,
        Supplier<Double> valueSupplier) {

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
