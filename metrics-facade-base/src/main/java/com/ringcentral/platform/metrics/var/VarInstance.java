package com.ringcentral.platform.metrics.var;

import com.ringcentral.platform.metrics.MetricInstance;

public interface VarInstance<V> extends MetricInstance {
    boolean isNonDecreasing();
}