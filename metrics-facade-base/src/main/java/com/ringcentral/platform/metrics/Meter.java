package com.ringcentral.platform.metrics;

import com.ringcentral.platform.metrics.labels.LabelValues;

public interface Meter extends Metric {
    void removeInstancesFor(LabelValues labelValues);
}
