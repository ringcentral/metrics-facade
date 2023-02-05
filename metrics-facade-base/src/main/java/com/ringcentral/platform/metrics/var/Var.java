package com.ringcentral.platform.metrics.var;

import com.ringcentral.platform.metrics.Metric;
import com.ringcentral.platform.metrics.labels.LabelValues;

import java.util.function.Supplier;

public interface Var<V> extends Metric {
    interface Value extends VarMeasurable {}

    void register(Supplier<V> valueSupplier, LabelValues labelValues);
    void deregister(LabelValues labelValues);

    static <V> Supplier<V> noTotal() {
        return null;
    }
}