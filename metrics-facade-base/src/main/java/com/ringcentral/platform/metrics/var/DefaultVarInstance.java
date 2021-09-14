package com.ringcentral.platform.metrics.var;

import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;

import java.util.List;
import java.util.function.Supplier;

public class DefaultVarInstance<V> extends AbstractVarInstance<V> {

    public DefaultVarInstance(
        MetricName name,
        List<MetricDimensionValue> dimensionValues,
        boolean totalInstance,
        boolean dimensionalTotalInstance,
        boolean nonDecreasing,
        Measurable valueMeasurable,
        Supplier<V> valueSupplier) {

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
