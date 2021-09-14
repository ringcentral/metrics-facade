package com.ringcentral.platform.metrics.var;

import com.ringcentral.platform.metrics.Metric;
import com.ringcentral.platform.metrics.dimensions.MetricDimensionValues;

import java.util.function.Supplier;

public interface Var<V> extends Metric {
    interface Value extends VarMeasurable {}

    void register(Supplier<V> valueSupplier, MetricDimensionValues dimensionValues);
    void deregister(MetricDimensionValues dimensionValues);

    static <V> Supplier<V> noTotal() {
        return null;
    }
}