package com.ringcentral.platform.metrics.var.objectVar;

import com.ringcentral.platform.metrics.dimensions.MetricDimensionValue;
import com.ringcentral.platform.metrics.measurables.Measurable;
import com.ringcentral.platform.metrics.names.MetricName;
import com.ringcentral.platform.metrics.var.*;

import java.util.List;
import java.util.function.Supplier;

public class DefaultObjectVarInstance extends AbstractVarInstance<Object> implements ObjectVarInstance {

    public DefaultObjectVarInstance(
        MetricName name,
        List<MetricDimensionValue> dimensionValues,
        boolean totalInstance,
        boolean dimensionalTotalInstance,
        boolean nonDecreasing,
        Measurable valueMeasurable,
        Supplier<Object> valueSupplier) {

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
